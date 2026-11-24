package com.example.cams.integration;

import com.example.cams.email.EmailTestSupport;
import com.example.cams.email.TestClockConfig;
import com.example.cams.entity.CamsAppointment;
import com.example.cams.repository.CamsAppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BUG-02: when two requests race for one slot, the database's one_active_appointment_per_slot index rejects
 * the loser. The API must answer 409 slot_taken, not 500 internal_error. H2 has no partial unique index, so the
 * violation is simulated at the repository; the real race is covered by system test TC-42 on PostgreSQL.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:slot-conflict;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class CamsSlotConflictTest extends EmailTestSupport {

    @MockitoSpyBean
    private CamsAppointmentRepository appointmentRepository;

    private static DataIntegrityViolationException slotIndexViolation() {
        return new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("ERROR: duplicate key value violates unique constraint "
                        + "\"one_active_appointment_per_slot\""));
    }

    @Test
    void losingTheRaceWhenBookingAnswers409SlotTaken() throws Exception {
        Session patient = newPatient();
        long slot = freeSlot("2026-11-27", "09:00");
        doThrow(slotIndexViolation()).when(appointmentRepository).saveAndFlush(any(CamsAppointment.class));

        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slot_id\":\"%d\",\"reason\":\"Kham\"}".formatted(slot)).header("Host", HOST)
                        .header("X-CSRF-Token", patient.csrf()).cookie(patient.cookie()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("slot_taken"))
                .andExpect(jsonPath("$.error").value("Khung giờ vừa được người khác đặt. Vui lòng chọn lại."));
    }

    @Test
    void losingTheRaceWhenReschedulingAnswers409SlotTaken() throws Exception {
        Session patient = newPatient();
        long id = patientBooks(patient, freeSlot("2026-11-27", "09:30"));
        long other = freeSlot("2026-11-27", "10:00");
        doThrow(slotIndexViolation()).when(appointmentRepository).saveAndFlush(any(CamsAppointment.class));

        MvcResult result = transition(patient, id, "reschedule", "{\"slot_id\":%d,\"reason\":\"Doi\"}".formatted(other));

        assertEquals(409, result.getResponse().getStatus());
        assertEquals("slot_taken", json(result).get("code").asText());
    }

    @Test
    void otherIntegrityErrorsAreStillInternalErrors() throws Exception {
        Session patient = newPatient();
        long slot = freeSlot("2026-11-27", "10:30");
        doThrow(new DataIntegrityViolationException("some other constraint")).when(appointmentRepository)
                .saveAndFlush(any(CamsAppointment.class));

        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slot_id\":\"%d\",\"reason\":\"Kham\"}".formatted(slot)).header("Host", HOST)
                        .header("X-CSRF-Token", patient.csrf()).cookie(patient.cookie()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("internal_error"));
    }
}
