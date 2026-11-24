package com.example.cams.email;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.servlet.MvcResult;
import com.example.cams.repository.CamsAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** SC7 / FR-17 with mail enabled: confirmation emails, reminders, endpoints and settings (H2, mocked mail sender). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sc7-email;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "cams.mail.enabled=true",
        "cams.mail.from=no-reply@cams.test" })
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class CamsEmailTest extends EmailTestSupport {

    @Autowired
    private CamsAppointmentRepository appointmentRepository;

    // ---- confirmation emails -------------------------------------------------------------------------

    /** Test 1: approving a pending appointment sends exactly one confirmation to the patient. */
    @Test
    void approvingAPendingAppointmentSendsOneConfirmationEmail() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-27", "09:00"));
        assertTrue(sentMail().isEmpty(), "booking alone (pending) must not send anything");

        approve(id);

        List<SimpleMailMessage> mails = sentMailTo(patient.email());
        assertEquals(1, mails.size());
        SimpleMailMessage mail = mails.get(0);
        assertEquals("no-reply@cams.test", mail.getFrom());
        assertTrue(isConfirmation(mail));
        String text = mail.getText();
        assertNotNull(text);
        assertTrue(text.contains("Nguyen Van Test"), text);
        assertTrue(text.contains("Bác sĩ Demo"), text);
        assertTrue(text.contains("Chuyên khoa:"), text);
        assertTrue(text.contains("27/11/2026 09:00"), text);
        assertTrue(text.contains("Kham tong quat"), text);
        assertTrue(text.contains("#" + id), text);
        assertTrue(text.contains("ít nhất 2 giờ"), text);

        JsonNode logs = emailLog(id);
        assertEquals(1, logs.size());
        assertEquals("confirm", logs.get(0).get("kind").asText());
        assertEquals("sent", logs.get(0).get("status").asText());
        assertEquals(patient.email(), logs.get(0).get("recipient").asText());
    }

    /** Test 2: a receptionist booking for a patient who has an account sends one confirmation. */
    @Test
    void receptionistBookingForAnAccountHolderSendsOneConfirmationEmail() throws Exception {
        Session patient = newPatient();
        long id = staffBooks("\"patient_name\":\"Nguyen Van Test\",\"patient_phone\":\"0911222333\","
                + "\"patient_email\":\"%s\"".formatted(patient.email()), freeSlot("2026-11-27", "09:30"));

        List<SimpleMailMessage> mails = sentMailTo(patient.email());
        assertEquals(1, mails.size());
        assertTrue(isConfirmation(mails.get(0)));
        assertTrue(mails.get(0).getText().contains("27/11/2026 09:30"));
        JsonNode logs = emailLog(id);
        assertEquals("sent", logs.get(0).get("status").asText());
    }

    /** Test 3: a walk-in guest has no email: nothing is sent, the log says skipped, the booking still succeeds. */
    @Test
    void guestWithoutEmailIsSkippedButBookingSucceeds() throws Exception {
        long id = staffBooks("\"patient_name\":\"Khach Vang Lai\",\"patient_phone\":\"0911222333\"",
                freeSlot("2026-11-27", "10:00"));

        assertTrue(sentMail().isEmpty());
        JsonNode logs = emailLog(id);
        assertEquals(1, logs.size());
        assertEquals("confirm", logs.get(0).get("kind").asText());
        assertEquals("skipped", logs.get(0).get("status").asText());
        mockMvc.perform(get("/api/appointments").header("Host", HOST).cookie(staff.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments[?(@.id==%d)].status".formatted(id)).value("confirmed"));
    }

    /** Test 4: a mail server error must not undo the approval; it is only recorded as failed. */
    @Test
    void sendFailureDoesNotBreakApproval() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-27", "10:30"));
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        MvcResult approved = transition(staff, id, "approve", "{}");

        assertEquals(200, approved.getResponse().getStatus());
        assertEquals("confirmed", json(approved).get("appointment").get("status").asText());
        JsonNode logs = emailLog(id);
        assertEquals(1, logs.size());
        assertEquals("failed", logs.get(0).get("status").asText());
        assertTrue(logs.get(0).get("error").asText().contains("SMTP down"));
        mockMvc.perform(get("/api/appointments").header("Host", HOST).cookie(patient.cookie()))
                .andExpect(jsonPath("$.appointments[0].status").value("confirmed"));
    }

    // ---- reminders -----------------------------------------------------------------------------------

    /**
     * Test 6: with the clock fixed at Monday 12:00 and reminder_hours = 24, the appointment 20 hours away is
     * reminded; the one 28 hours away (outside the window), the cancelled one and the pending one are not.
     * (A slot exactly 30 hours away would fall outside working hours, so 28 hours is used.)
     */
    @Test
    void reminderGoesOnlyToConfirmedAppointmentsInsideTheWindow() throws Exception {
        Session near = newPatient();
        Session far = newPatient();
        Session cancelled = newPatient();
        Session pending = newPatient();
        long nearId = patientBooks(near, freeSlot("2026-11-24", "08:00"));
        long farId = patientBooks(far, freeSlot("2026-11-24", "16:00"));
        long cancelledId = patientBooks(cancelled, freeSlot("2026-11-24", "08:30"));
        patientBooks(pending, freeSlot("2026-11-24", "09:00"));
        approve(nearId);
        approve(farId);
        approve(cancelledId);
        assertEquals(200, transition(staff, cancelledId, "cancel", "{\"reason\":\"Khach huy\"}").getResponse().getStatus());
        clearInvocations(mailSender);

        JsonNode result = runReminders();

        assertEquals(1, result.get("sent").asInt());
        assertEquals(0, result.get("skipped").asInt());
        List<SimpleMailMessage> reminders = sentMail().stream().filter(EmailTestSupport::isReminder).toList();
        assertEquals(1, reminders.size());
        assertEquals(near.email(), reminders.get(0).getTo()[0]);
        assertTrue(reminders.get(0).getText().contains("24/11/2026 08:00"));
        assertTrue(sentMailTo(far.email()).isEmpty());
        assertTrue(sentMailTo(cancelled.email()).isEmpty());
        assertTrue(sentMailTo(pending.email()).isEmpty());
        assertEquals("reminder", emailLog(nearId).get(emailLog(nearId).size() - 1).get("kind").asText());
        assertNotNull(appointmentRepository.findById(nearId).orElseThrow().getReminderSentAt());
    }

    /** Test 7: running the reminder job again never sends a second reminder. */
    @Test
    void runningTheReminderJobTwiceSendsOneReminderPerAppointment() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "10:00"));
        approve(id);
        clearInvocations(mailSender);

        JsonNode first = runReminders();
        JsonNode second = runReminders();

        assertEquals(1, first.get("sent").asInt());
        assertEquals(0, second.get("sent").asInt());
        assertEquals(1, sentMailTo(patient.email()).stream().filter(EmailTestSupport::isReminder).count());
    }

    /** Test 8: rescheduling re-sends the confirmation, clears reminder_sent_at and is reminded for the new time. */
    @Test
    void reschedulingResendsConfirmationAndRemindsAgain() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "10:30"));
        approve(id);
        runReminders();
        assertNotNull(appointmentRepository.findById(id).orElseThrow().getReminderSentAt());
        clearInvocations(mailSender);

        long newSlot = freeSlot("2026-11-24", "11:00");
        MvcResult moved = transition(patient, id, "reschedule", "{\"slot_id\":%d,\"reason\":\"Doi gio\"}".formatted(newSlot));
        assertEquals(200, moved.getResponse().getStatus());

        List<SimpleMailMessage> afterMove = sentMailTo(patient.email());
        assertEquals(1, afterMove.size());
        assertTrue(isConfirmation(afterMove.get(0)));
        assertTrue(afterMove.get(0).getText().contains("24/11/2026 11:00"), afterMove.get(0).getText());
        assertNull(appointmentRepository.findById(id).orElseThrow().getReminderSentAt());

        JsonNode again = runReminders();
        assertEquals(1, again.get("sent").asInt());
        List<SimpleMailMessage> reminders = sentMailTo(patient.email()).stream().filter(EmailTestSupport::isReminder).toList();
        assertEquals(1, reminders.size());
        assertTrue(reminders.get(0).getText().contains("24/11/2026 11:00"));
    }

    /** An appointment that has already arrived is no longer "confirmed", so it is not reminded. */
    @Test
    void arrivedAppointmentIsNotReminded() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "13:00"));
        approve(id);
        clock.set("2026-11-24T07:00:00+07:00");
        assertEquals(200, transition(staff, id, "checkin", "{}").getResponse().getStatus());
        clearInvocations(mailSender);

        runReminders();

        assertTrue(sentMailTo(patient.email()).isEmpty());
    }

    /** A failed reminder is retried by the next run instead of being lost. */
    @Test
    void failedReminderIsRetriedOnTheNextRun() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "07:30"));
        approve(id);
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        JsonNode failed = runReminders();
        assertEquals(0, failed.get("sent").asInt());
        assertNull(appointmentRepository.findById(id).orElseThrow().getReminderSentAt());

        org.mockito.Mockito.reset(mailSender);
        JsonNode retried = runReminders();
        assertEquals(1, retried.get("sent").asInt());
        assertEquals(1, sentMailTo(patient.email()).stream().filter(EmailTestSupport::isReminder).count());
    }

    // ---- endpoint: POST /api/admin/reminders/run --------------------------------------------------------

    /** Test 9: only managers may run the job; other roles get 403, a missing CSRF token is refused. */
    @Test
    void onlyManagersMayRunTheReminderJob() throws Exception {
        Session patient = newPatient();
        Session doctor = login("doctor01@cams.local", "Demo@12345");
        for (Session other : List.of(patient, staff, doctor)) {
            mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST)
                            .header("X-CSRF-Token", other.csrf()).cookie(other.cookie()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("forbidden"));
        }
        mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST).cookie(manager.cookie()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("csrf_failed"));
        mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST)
                        .header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").isNumber())
                .andExpect(jsonPath("$.skipped").isNumber());
        mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST))
                .andExpect(status().isUnauthorized());
    }

    /** The cross-origin guard applies to the new write endpoint like every other write API. */
    @Test
    void reminderRunRejectsAForeignOrigin() throws Exception {
        mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST).header("Origin", "http://evil.example")
                        .header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().is4xxClientError());
    }

    /** GET /api/admin/email-log is manager-only. */
    @Test
    void emailLogIsManagerOnly() throws Exception {
        Session patient = newPatient();
        mockMvc.perform(get("/api/admin/email-log").header("Host", HOST).cookie(patient.cookie()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/email-log").header("Host", HOST).cookie(staff.cookie()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/email-log").header("Host", HOST).cookie(manager.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray());
    }

    // ---- setting: reminder_hours --------------------------------------------------------------------

    /** Test 10: reminder_hours must be 1-72 (0 and 73 are refused, 12 is accepted). */
    @Test
    void reminderHoursSettingIsBounded() throws Exception {
        try {
            for (String bad : List.of("0", "73")) {
                postSettings("{\"reminder_hours\":%s}".formatted(bad)).andExpect(status().isBadRequest());
            }
            postSettings("{\"reminder_hours\":12}").andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
            mockMvc.perform(get("/api/session").header("Host", HOST).cookie(manager.cookie()))
                    .andExpect(jsonPath("$.settings.reminder_hours").value(12));
        } finally {
            postSettings("{\"reminder_hours\":24}").andExpect(status().isOk());
        }
    }

    /** The configured window is honoured: with reminder_hours = 1 the appointment 20 hours away is not reminded. */
    @Test
    void reminderWindowFollowsTheSetting() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "07:00"));
        approve(id);
        clearInvocations(mailSender);
        try {
            postSettings("{\"reminder_hours\":1}").andExpect(status().isOk());
            assertEquals(0, runReminders().get("sent").asInt());
            assertFalse(sentMailTo(patient.email()).stream().anyMatch(EmailTestSupport::isReminder));
        } finally {
            postSettings("{\"reminder_hours\":24}").andExpect(status().isOk());
        }
        assertEquals(1, runReminders().get("sent").asInt());
    }

    private org.springframework.test.web.servlet.ResultActions postSettings(String body) throws Exception {
        return mockMvc.perform(post("/api/settings").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Host", HOST).header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()));
    }
}
