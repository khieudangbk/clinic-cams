package com.example.cams.service;

import com.example.cams.dto.response.admin.ReminderRunResponse;
import com.example.cams.entity.CamsAppointment;
import com.example.cams.entity.CamsEmailLog;
import com.example.cams.entity.enums.AppointmentStatus;
import com.example.cams.repository.CamsAppointmentRepository;
import com.example.cams.util.CamsClock;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * SC7: sends one reminder email per confirmed appointment whose slot starts within the next
 * reminder_hours. Each appointment is claimed atomically (reminder_sent_at) before sending, so running
 * the job again, or two runs at once, never produces a second reminder.
 */
@Service
public class CamsReminderService {

    private final CamsAppointmentRepository appointmentRepository;
    private final CamsEmailService emailService;
    private final CamsCatalogService catalogService;
    private final CamsClock clock;

    public CamsReminderService(CamsAppointmentRepository appointmentRepository, CamsEmailService emailService,
            CamsCatalogService catalogService, CamsClock clock) {
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
        this.catalogService = catalogService;
        this.clock = clock;
    }

    public ReminderRunResponse run() {
        OffsetDateTime now = clock.now();
        int reminderHours = catalogService.settingsMap().getOrDefault("reminder_hours", 24);
        List<CamsAppointment> due = appointmentRepository.findDueForReminder(AppointmentStatus.confirmed, now,
                now.plusHours(reminderHours));
        int sent = 0;
        int skipped = 0;
        for (CamsAppointment appointment : due) {
            if (appointmentRepository.claimReminder(appointment.getId(), now) == 0) {
                continue; // another run took it
            }
            String status = emailService.deliver(appointment.getId(), CamsEmailLog.KIND_REMINDER);
            if (CamsEmailLog.STATUS_SENT.equals(status)) {
                sent++;
            } else {
                skipped++;
                if (CamsEmailLog.STATUS_FAILED.equals(status)) {
                    appointmentRepository.releaseReminder(appointment.getId()); // retry on the next run
                }
            }
        }
        return new ReminderRunResponse(sent, skipped);
    }
}
