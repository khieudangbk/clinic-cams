package com.example.cams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end smoke test for the CAMS booking API added alongside the generic auth module:
 * register, login, read the public catalog, list a doctor's free slots and book one as a
 * patient. Exercises the cams_session cookie + X-CSRF-Token flow the React frontend relies on.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CamsBookingFlowTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndBookASlot() throws Exception {
        String email = "patient-" + System.nanoTime() + "@example.com";
        String registerBody = """
                {"name":"Test Patient","email":"%s","password":"Testing@12345"}
                """.formatted(email);
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(registerBody)
                        .header("Host", "127.0.0.1"))
                .andExpect(status().isCreated());

        String loginBody = """
                {"email":"%s","password":"Testing@12345"}
                """.formatted(email);
        MvcResult loginResult = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody).header("Host", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("patient"))
                .andExpect(jsonPath("$.csrf").exists())
                .andReturn();
        JsonNode login = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String csrf = login.get("csrf").asText();
        var sessionCookie = loginResult.getResponse().getCookie("cams_session");

        MvcResult catalogResult = mockMvc.perform(get("/api/public/catalog").header("Host", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode catalog = objectMapper.readTree(catalogResult.getResponse().getContentAsString());
        long doctorId = catalog.get("doctors").get(0).get("id").asLong();

        String day = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        MvcResult slotsResult = mockMvc.perform(get("/api/public/slots")
                        .param("doctor_id", String.valueOf(doctorId)).param("day", day)
                        .header("Host", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode slots = objectMapper.readTree(slotsResult.getResponse().getContentAsString()).get("slots");
        long slotId = slots.get(0).get("id").asLong();

        String bookingBody = """
                {"slot_id":"%d","reason":"Khám tổng quát","booking_details":{
                    "booking_for":"self","name":"Test Patient","phone":"0912345678"
                }}
                """.formatted(slotId);
        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(bookingBody)
                        .header("Host", "127.0.0.1").header("X-CSRF-Token", csrf).cookie(sessionCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointment.status").value("pending"))
                .andExpect(jsonPath("$.appointment.patient_phone").value("0912345678"));
    }

    private record Session(String csrf, jakarta.servlet.http.Cookie cookie) {
    }

    private Session login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        MvcResult result = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", "127.0.0.1"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Session(json.get("csrf").asText(), result.getResponse().getCookie("cams_session"));
    }

    /** Receptionist books for a guest, then cancels it; manager updates settings/schedule/leave. */
    @Test
    void receptionistAndManagerFlow() throws Exception {
        Session staff = login("staff@cams.local", "Demo@12345");

        MvcResult catalogResult = mockMvc.perform(get("/api/catalog").header("Host", "127.0.0.1")
                        .cookie(staff.cookie()))
                .andExpect(status().isOk())
                .andReturn();
        long doctorId = objectMapper.readTree(catalogResult.getResponse().getContentAsString())
                .get("doctors").get(0).get("id").asLong();

        String day = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE);
        MvcResult slotsResult = mockMvc.perform(get("/api/slots").param("doctor_id", String.valueOf(doctorId))
                        .param("day", day).header("Host", "127.0.0.1").cookie(staff.cookie()))
                .andExpect(status().isOk())
                .andReturn();
        long slotId = objectMapper.readTree(slotsResult.getResponse().getContentAsString())
                .get("slots").get(0).get("id").asLong();

        String bookingBody = """
                {"slot_id":%d,"reason":"Tai mui hong","patient_name":"Khach Via","patient_phone":"0911222333"}
                """.formatted(slotId);
        MvcResult createResult = mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(bookingBody).header("Host", "127.0.0.1").header("X-CSRF-Token", staff.csrf())
                        .cookie(staff.cookie()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointment.status").value("confirmed"))
                .andExpect(jsonPath("$.appointment.channel").value("staff"))
                .andReturn();
        long appointmentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("appointment").get("id").asLong();

        mockMvc.perform(post("/api/appointments/" + appointmentId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Khach doi y\"}")
                        .header("Host", "127.0.0.1").header("X-CSRF-Token", staff.csrf()).cookie(staff.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointment.status").value("cancelled"));

        mockMvc.perform(get("/api/appointments/" + appointmentId + "/audit").header("Host", "127.0.0.1")
                        .cookie(staff.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audit.length()").value(2));

        Session manager = login("admin@cams.local", "Demo@12345");

        mockMvc.perform(get("/api/users").header("Host", "127.0.0.1").cookie(manager.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());

        mockMvc.perform(post("/api/settings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cutoff_hours\":3}").header("Host", "127.0.0.1")
                        .header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        mockMvc.perform(post("/api/schedules").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctor_id\":%d,\"weekdays\":[0,1,2,3,4],\"start_time\":\"08:00\",\"end_time\":\"16:00\"}"
                                .formatted(doctorId))
                        .header("Host", "127.0.0.1").header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        String leaveDay = LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
        mockMvc.perform(post("/api/leaves").contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"doctor_id\":%d,\"starts_at\":\"%sT08:00:00+07:00\","
                                + "\"ends_at\":\"%sT12:00:00+07:00\",\"reason\":\"Nghi om\"}")
                                .formatted(doctorId, leaveDay, leaveDay))
                        .header("Host", "127.0.0.1").header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.affected").isArray());
    }
}
