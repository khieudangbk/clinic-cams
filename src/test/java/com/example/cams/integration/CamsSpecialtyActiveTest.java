package com.example.cams.integration;

import com.example.cams.email.EmailTestSupport;
import com.example.cams.email.TestClockConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** BUG-01 / TC-12 (FR-16): a manager can stop a specialty; it then disappears from the catalog and is not bookable. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:specialty-active;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" })
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
class CamsSpecialtyActiveTest extends EmailTestSupport {

    private static final String DAY = "2026-11-27";

    private ResultActions postJson(Session who, String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body).header("Host", HOST)
                .header("X-CSRF-Token", who.csrf()).cookie(who.cookie()));
    }

    private boolean listed(JsonNode catalog, String arrayName, String field, String value) {
        for (JsonNode item : catalog.get(arrayName)) {
            if (value.equals(item.get(field).asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode publicCatalog() throws Exception {
        return json(mockMvc.perform(get("/api/public/catalog").header("Host", HOST)).andExpect(status().isOk()).andReturn());
    }

    private JsonNode managerCatalog() throws Exception {
        return json(mockMvc.perform(get("/api/catalog").header("Host", HOST).cookie(manager.cookie()))
                .andExpect(status().isOk()).andReturn());
    }

    /** Creates a specialty with one doctor and returns {specialtyId, doctorId}. */
    private long[] newSpecialtyWithDoctor(String name) throws Exception {
        postJson(manager, "/api/specialties", "{\"name\":\"%s\"}".formatted(name)).andExpect(status().isOk());
        long specialtyId = 0;
        for (JsonNode s : managerCatalog().get("specialties")) {
            if (name.equals(s.get("name").asText())) {
                specialtyId = s.get("id").asLong();
            }
        }
        assertTrue(specialtyId > 0);
        String email = "doctor-" + System.nanoTime() + "@cams.local";
        long doctorId = json(postJson(manager, "/api/users", ("{\"role\":\"doctor\",\"name\":\"BS %s\",\"email\":\"%s\","
                + "\"password\":\"Testing@12345\",\"specialty_id\":%d}").formatted(name, email, specialtyId))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
        return new long[] { specialtyId, doctorId };
    }

    private long slotOf(long doctorId) throws Exception {
        JsonNode slots = json(mockMvc.perform(get("/api/public/slots").param("doctor_id", String.valueOf(doctorId))
                .param("day", DAY).header("Host", HOST)).andExpect(status().isOk()).andReturn()).get("slots");
        return slots.get(0).get("id").asLong();
    }

    @Test
    void newSpecialtyIsActiveAndAppearsInTheCatalog() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Moi");

        assertTrue(listed(publicCatalog(), "specialties", "name", "CK Moi"));
        assertTrue(listed(publicCatalog(), "doctors", "name", "BS CK Moi"));
        for (JsonNode s : managerCatalog().get("specialties")) {
            if (s.get("id").asLong() == ids[0]) {
                assertEquals(1, s.get("active").asInt());
            }
        }
        assertFalse(publicCatalog().get("specialties").get(0).has("active"), "public catalog keeps its old shape");
    }

    @Test
    void stoppingASpecialtyHidesItAndItsDoctorsAndBlocksBooking() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Ngung");
        Session patient = newPatient();
        long slot = slotOf(ids[1]);
        long keptAppointment = patientBooks(patient, freeSlotOfDoctor(ids[1], "09:00"));

        postJson(manager, "/api/specialties", "{\"id\":%d,\"active\":false}".formatted(ids[0]))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));

        // gone from the public catalog (specialty and its doctor), and from a staff member's catalog
        assertFalse(listed(publicCatalog(), "specialties", "name", "CK Ngung"));
        assertFalse(listed(publicCatalog(), "doctors", "name", "BS CK Ngung"));
        JsonNode staffCatalog = json(mockMvc.perform(get("/api/catalog").header("Host", HOST).cookie(staff.cookie()))
                .andExpect(status().isOk()).andReturn());
        assertFalse(listed(staffCatalog, "specialties", "name", "CK Ngung"));
        // the manager still sees it, marked stopped, with its name untouched
        boolean seenStopped = false;
        for (JsonNode s : managerCatalog().get("specialties")) {
            if (s.get("id").asLong() == ids[0]) {
                assertEquals("CK Ngung", s.get("name").asText());
                assertEquals(0, s.get("active").asInt());
                seenStopped = true;
            }
        }
        assertTrue(seenStopped);
        // its doctor can no longer be browsed or booked
        mockMvc.perform(get("/api/public/slots").param("doctor_id", String.valueOf(ids[1])).param("day", DAY)
                .header("Host", HOST)).andExpect(status().isNotFound());
        postJson(newPatient(), "/api/appointments", "{\"slot_id\":\"%d\",\"reason\":\"Kham\"}".formatted(slot))
                .andExpect(status().isNotFound());
        // other specialties are untouched
        assertTrue(publicCatalog().get("specialties").size() >= 3);
        // appointments made earlier stay visible
        mockMvc.perform(get("/api/appointments").header("Host", HOST).cookie(patient.cookie()))
                .andExpect(jsonPath("$.appointments[?(@.id==%d)].status".formatted(keptAppointment)).value("pending"));
    }

    @Test
    void reopeningASpecialtyBringsItBack() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Mo Lai");
        postJson(manager, "/api/specialties", "{\"id\":%d,\"active\":0}".formatted(ids[0])).andExpect(status().isOk());
        assertFalse(listed(publicCatalog(), "specialties", "name", "CK Mo Lai"));

        postJson(manager, "/api/specialties", "{\"id\":%d,\"active\":1}".formatted(ids[0])).andExpect(status().isOk());

        assertTrue(listed(publicCatalog(), "specialties", "name", "CK Mo Lai"));
        assertTrue(listed(publicCatalog(), "doctors", "name", "BS CK Mo Lai"));
        assertNotNull(slotOf(ids[1]));
    }

    @Test
    void renamingStillWorksAndNeedsNoActiveFlag() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Doi Ten");

        postJson(manager, "/api/specialties", "{\"id\":%d,\"name\":\"CK Da Doi\"}".formatted(ids[0])).andExpect(status().isOk());

        assertTrue(listed(publicCatalog(), "specialties", "name", "CK Da Doi"));
        assertFalse(listed(publicCatalog(), "specialties", "name", "CK Doi Ten"));
    }

    @Test
    void onlyManagersCanChangeASpecialtyAndInputIsValidated() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Bao Ve");
        postJson(staff, "/api/specialties", "{\"id\":%d,\"active\":false}".formatted(ids[0]))
                .andExpect(status().isForbidden());
        postJson(newPatient(), "/api/specialties", "{\"id\":%d,\"active\":false}".formatted(ids[0]))
                .andExpect(status().isForbidden());
        postJson(manager, "/api/specialties", "{\"id\":%d,\"active\":\"yes\"}".formatted(ids[0]))
                .andExpect(status().isBadRequest());
        postJson(manager, "/api/specialties", "{\"id\":999999,\"active\":false}").andExpect(status().isNotFound());
        postJson(manager, "/api/specialties", "{\"active\":false}").andExpect(status().isBadRequest());
        assertTrue(listed(publicCatalog(), "specialties", "name", "CK Bao Ve"));
    }

    @Test
    void doctorsCannotBeAddedToAStoppedSpecialty() throws Exception {
        long[] ids = newSpecialtyWithDoctor("CK Khoa Cua");
        postJson(manager, "/api/specialties", "{\"id\":%d,\"active\":false}".formatted(ids[0])).andExpect(status().isOk());

        postJson(manager, "/api/users", ("{\"role\":\"doctor\",\"name\":\"BS Moi\",\"email\":\"x%d@cams.local\","
                + "\"password\":\"Testing@12345\",\"specialty_id\":%d}").formatted(System.nanoTime(), ids[0]))
                .andExpect(status().isBadRequest());
    }

    private long freeSlotOfDoctor(long doctorId, String hhmm) throws Exception {
        JsonNode slots = json(mockMvc.perform(get("/api/public/slots").param("doctor_id", String.valueOf(doctorId))
                .param("day", DAY).header("Host", HOST)).andExpect(status().isOk()).andReturn()).get("slots");
        for (JsonNode slot : slots) {
            if (slot.get("starts_at").asText().startsWith(DAY + "T" + hhmm)) {
                return slot.get("id").asLong();
            }
        }
        throw new IllegalStateException("slot " + hhmm);
    }
}
