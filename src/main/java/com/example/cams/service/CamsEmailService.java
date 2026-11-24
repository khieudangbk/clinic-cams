package com.example.cams.service;

import com.example.cams.dto.response.admin.EmailLogEntry;
import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.CamsEmailLog;
import com.example.cams.entity.CamsSlot;
import com.example.cams.entity.CamsSpecialty;
import com.example.cams.entity.CamsUser;
import com.example.cams.repository.CamsAppointmentRepository;
import com.example.cams.repository.CamsEmailLogRepository;
import com.example.cams.repository.CamsSlotRepository;
import com.example.cams.repository.CamsSpecialtyRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.service.event.AppointmentConfirmedEvent;
import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

/**
 * SC7: composes, sends and records confirmation and reminder emails. Sending never throws: any failure
 * is logged (WARN) and written to cams_email_log so booking, approval and rescheduling are unaffected.
 */
@Service
public class CamsEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(CamsEmailService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObjectProvider<JavaMailSender> mailSender;
    private final CamsAppointmentRepository appointmentRepository;
    private final CamsSlotRepository slotRepository;
    private final CamsUserRepository userRepository;
    private final CamsSpecialtyRepository specialtyRepository;
    private final CamsEmailLogRepository emailLogRepository;
    private final CamsCatalogService catalogService;
    private final CamsClock clock;
    private final TransactionTemplate newTransaction;
    private final boolean enabled;
    private final String from;

    public CamsEmailService(ObjectProvider<JavaMailSender> mailSender, CamsAppointmentRepository appointmentRepository,
            CamsSlotRepository slotRepository, CamsUserRepository userRepository,
            CamsSpecialtyRepository specialtyRepository, CamsEmailLogRepository emailLogRepository,
            CamsCatalogService catalogService, CamsClock clock, PlatformTransactionManager transactionManager,
            @Value("${cams.mail.enabled:false}") boolean enabled,
            @Value("${cams.mail.from:no-reply@cams.local}") String from) {
        this.mailSender = mailSender;
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.emailLogRepository = emailLogRepository;
        this.catalogService = catalogService;
        this.clock = clock;
        this.enabled = enabled;
        this.from = from;
        // AFTER_COMMIT listeners still see the finished transaction bound to the thread, so every database
        // access here opens its own transaction instead of joining it.
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Sent once the booking transaction has committed; a rollback therefore sends nothing. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConfirmed(AppointmentConfirmedEvent event) {
        deliver(event.appointmentId(), CamsEmailLog.KIND_CONFIRM);
    }

    /** Sends one email of the given kind and returns the status written to the log. Never throws. */
    public String deliver(long appointmentId, String kind) {
        Draft draft;
        try {
            draft = inNewTransaction(() -> compose(appointmentId, kind));
        } catch (RuntimeException e) {
            LOG.warn("Không soạn được email {} cho lịch {}: {}", kind, appointmentId, e.toString());
            record(appointmentId, kind, "", CamsEmailLog.STATUS_FAILED, e.toString());
            return CamsEmailLog.STATUS_FAILED;
        }
        if (draft.recipient().isEmpty()) {
            LOG.info("Lịch {} không có email người nhận, bỏ qua email {}.", appointmentId, kind);
            record(appointmentId, kind, "", CamsEmailLog.STATUS_SKIPPED, "");
            return CamsEmailLog.STATUS_SKIPPED;
        }
        if (!enabled) {
            LOG.info("Email bị tắt (MAIL_ENABLED=false), không gửi email {} cho lịch {}.", kind, appointmentId);
            record(appointmentId, kind, draft.recipient(), CamsEmailLog.STATUS_DISABLED, "");
            return CamsEmailLog.STATUS_DISABLED;
        }
        try {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                throw new IllegalStateException("Chưa cấu hình SMTP (MAIL_HOST).");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(draft.recipient());
            message.setSubject(draft.subject());
            message.setText(draft.body());
            sender.send(message);
        } catch (Exception e) {
            LOG.warn("Gửi email {} cho lịch {} tới {} thất bại: {}", kind, appointmentId, draft.recipient(),
                    e.toString());
            record(appointmentId, kind, draft.recipient(), CamsEmailLog.STATUS_FAILED, e.toString());
            return CamsEmailLog.STATUS_FAILED;
        }
        record(appointmentId, kind, draft.recipient(), CamsEmailLog.STATUS_SENT, "");
        return CamsEmailLog.STATUS_SENT;
    }

    /** Manager view of cams_email_log: one appointment, or the 200 most recent rows. */
    public List<EmailLogEntry> logs(Long appointmentId) {
        List<CamsEmailLog> rows = appointmentId == null
                ? emailLogRepository.findTop200ByOrderByIdDesc()
                : emailLogRepository.findByAppointmentIdOrderByIdAsc(appointmentId);
        return rows.stream().map(row -> new EmailLogEntry(row.getId(), row.getAppointmentId(), row.getKind(),
                row.getRecipient(), row.getStatus(), row.getError(), CamsTime.stamp(row.getCreatedAt()))).toList();
    }

    private record Draft(String recipient, String subject, String body) {
    }

    private Draft compose(long appointmentId, String kind) {
        CamsAppointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        CamsSlot slot = slotRepository.findById(appointment.getSlotId()).orElseThrow();
        CamsUser patient = userRepository.findById(appointment.getPatientId()).orElseThrow();
        CamsUser doctor = userRepository.findById(slot.getDoctorId()).orElseThrow();
        String specialty = doctor.getSpecialtyId() == null ? ""
                : specialtyRepository.findById(doctor.getSpecialtyId()).map(CamsSpecialty::getName).orElse("");
        int cutoffHours = catalogService.settingsMap().getOrDefault("cutoff_hours", 2);

        String recipient = patient.getEmail() == null ? "" : patient.getEmail().trim();
        String patientName = appointment.getPatientNameOverride().isEmpty() ? patient.getName()
                : appointment.getPatientNameOverride();
        String when = slot.getStartsAt().withOffsetSameInstant(CamsTime.OFFSET).format(WHEN);
        boolean reminder = CamsEmailLog.KIND_REMINDER.equals(kind);

        String subject = (reminder ? "[CAMS] Nhắc lịch khám #" : "[CAMS] Xác nhận lịch khám #") + appointment.getId();
        String body = "Xin chào " + patientName + ",\n\n"
                + (reminder ? "Phòng khám CAMS nhắc bạn sắp đến lịch khám sau.\n\n"
                        : "Lịch khám của bạn tại phòng khám CAMS đã được xác nhận.\n\n")
                + "Mã lịch: #" + appointment.getId() + "\n"
                + "Bác sĩ: " + doctor.getName() + "\n"
                + (specialty.isEmpty() ? "" : "Chuyên khoa: " + specialty + "\n")
                + "Thời gian khám: " + when + "\n"
                + "Lý do khám: " + appointment.getReason() + "\n\n"
                + "Bạn có thể đổi hoặc hủy lịch trước giờ hẹn ít nhất " + cutoffHours + " giờ.\n\n"
                + "Phòng khám đa khoa CAMS";
        return new Draft(recipient, subject, body);
    }

    private void record(long appointmentId, String kind, String recipient, String status, String error) {
        try {
            inNewTransaction(() -> {
                CamsEmailLog row = new CamsEmailLog();
                row.setAppointmentId(appointmentId);
                row.setKind(kind);
                row.setRecipient(recipient.length() > 150 ? recipient.substring(0, 150) : recipient);
                row.setStatus(status);
                row.setError(error.length() > 500 ? error.substring(0, 500) : error);
                row.setCreatedAt(clock.now());
                return emailLogRepository.save(row);
            });
        } catch (RuntimeException e) {
            LOG.warn("Không ghi được cams_email_log cho lịch {}: {}", appointmentId, e.toString());
        }
    }

    private <T> T inNewTransaction(Supplier<T> work) {
        return newTransaction.execute(status -> work.get());
    }
}
