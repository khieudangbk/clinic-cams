package com.example.cams.email;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** SC7 / FR-17 with MAIL_ENABLED=false: nothing is sent, everything is logged as disabled. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sc7-email-off;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "cams.mail.enabled=false" })
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class CamsEmailDisabledTest extends EmailTestSupport {

    /** Test 5: approval succeeds, the mail sender is never called and the log says disabled. */
    @Test
    void disabledMailSendsNothingAndLogsDisabled() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-27", "09:00"));

        approve(id);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        assertTrue(sentMail().isEmpty());
        JsonNode logs = emailLog(id);
        assertEquals(1, logs.size());
        assertEquals("confirm", logs.get(0).get("kind").asText());
        assertEquals("disabled", logs.get(0).get("status").asText());
        assertEquals(patient.email(), logs.get(0).get("recipient").asText());
    }

    /** TC-51 at unit level: a disabled reminder is still marked handled, so it is not retried every run. */
    @Test
    void disabledReminderIsMarkedHandled() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-24", "08:00"));
        approve(id);

        JsonNode first = runReminders();
        JsonNode second = runReminders();

        assertEquals(0, first.get("sent").asInt());
        assertTrue(first.get("skipped").asInt() >= 1);
        assertEquals(0, second.get("skipped").asInt());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        JsonNode logs = emailLog(id);
        assertEquals("disabled", logs.get(logs.size() - 1).get("status").asText());
        assertEquals("reminder", logs.get(logs.size() - 1).get("kind").asText());
        assertNotNull(logs);
    }
}
