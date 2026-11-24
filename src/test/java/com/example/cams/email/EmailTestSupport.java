package com.example.cams.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared helpers for the SC7 email tests: sessions, slots, booking calls and the sent-mail list. */
abstract class EmailTestSupport {

    static final String HOST = "127.0.0.1";

    /** Monday 12:00 (+07:00). Tuesday 08:00 is 20 hours later, Tuesday 16:00 is 28 hours later. */
    static final String MONDAY_NOON = "2026-11-23T12:00:00+07:00";

    record Session(String csrf, Cookie cookie, String email) {
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TestClock clock;
    @MockitoBean
    protected JavaMailSender mailSender;

    protected Session staff;
    protected Session manager;

    @BeforeEach
    void resetClockAndLogin() throws Exception {
        clock.set(MONDAY_NOON);
        staff = login("staff@cams.local", "Demo@12345");
        manager = login("admin@cams.local", "Demo@12345");
    }

    protected Session login(String email, String password) throws Exception {
        String body = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
        MvcResult result = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST))
                .andExpect(status().isOk()).andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Session(json.get("csrf").asText(), result.getResponse().getCookie("cams_session"), email);
    }

    /** Registers a brand-new patient account (so the max-active limit never interferes) and logs in. */
    protected Session newPatient() throws Exception {
        String email = "sc7-" + System.nanoTime() + "@example.com";
        String body = "{\"name\":\"Nguyen Van Test\",\"email\":\"%s\",\"password\":\"Testing@12345\"}".formatted(email);
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST))
                .andExpect(status().isCreated());
        return login(email, "Testing@12345");
    }

    /** First free slot starting at hhmm on the given day, taken from whichever doctor still has it free. */
    protected long freeSlot(String day, String hhmm) throws Exception {
        JsonNode catalog = json(mockMvc.perform(get("/api/public/catalog").header("Host", HOST))
                .andExpect(status().isOk()).andReturn());
        for (JsonNode doctor : catalog.get("doctors")) {
            JsonNode slots = json(mockMvc.perform(get("/api/public/slots")
                            .param("doctor_id", doctor.get("id").asText()).param("day", day).header("Host", HOST))
                    .andExpect(status().isOk()).andReturn()).get("slots");
            for (JsonNode slot : slots) {
                if (slot.get("starts_at").asText().startsWith(day + "T" + hhmm)) {
                    return slot.get("id").asLong();
                }
            }
        }
        throw new IllegalStateException("Không còn khung giờ trống " + day + " " + hhmm);
    }

    protected long patientBooks(Session patient, long slotId) throws Exception {
        String body = "{\"slot_id\":\"%d\",\"reason\":\"Kham tong quat\"}".formatted(slotId);
        return appointmentId(mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(body).header("Host", HOST).header("X-CSRF-Token", patient.csrf())
                        .cookie(patient.cookie()))
                .andExpect(status().isCreated()).andReturn());
    }

    protected long staffBooks(String extraJson, long slotId) throws Exception {
        String body = "{\"slot_id\":%d,\"reason\":\"Dat ho tai quay\",%s}".formatted(slotId, extraJson);
        return appointmentId(mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content(body).header("Host", HOST).header("X-CSRF-Token", staff.csrf())
                        .cookie(staff.cookie()))
                .andExpect(status().isCreated()).andReturn());
    }

    protected MvcResult transition(Session actor, long appointmentId, String action, String body) throws Exception {
        return mockMvc.perform(post("/api/appointments/" + appointmentId + "/" + action)
                        .contentType(MediaType.APPLICATION_JSON).content(body).header("Host", HOST)
                        .header("X-CSRF-Token", actor.csrf()).cookie(actor.cookie()))
                .andReturn();
    }

    protected void approve(long appointmentId) throws Exception {
        mockMvc.perform(post("/api/appointments/" + appointmentId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON).content("{}").header("Host", HOST)
                        .header("X-CSRF-Token", staff.csrf()).cookie(staff.cookie()))
                .andExpect(status().isOk());
    }

    protected JsonNode runReminders() throws Exception {
        return json(mockMvc.perform(post("/api/admin/reminders/run").header("Host", HOST)
                        .header("X-CSRF-Token", manager.csrf()).cookie(manager.cookie()))
                .andExpect(status().isOk()).andReturn());
    }

    /** cams_email_log rows of one appointment, read through the manager endpoint. */
    protected JsonNode emailLog(long appointmentId) throws Exception {
        return json(mockMvc.perform(get("/api/admin/email-log").param("appointment_id", String.valueOf(appointmentId))
                        .header("Host", HOST).cookie(manager.cookie()))
                .andExpect(status().isOk()).andReturn()).get("logs");
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected long appointmentId(MvcResult result) throws Exception {
        return json(result).get("appointment").get("id").asLong();
    }

    /** Every SimpleMailMessage handed to the mocked JavaMailSender so far. */
    protected List<SimpleMailMessage> sentMail() {
        return mockingDetails(mailSender).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("send"))
                .flatMap(call -> Arrays.stream(call.getArguments()))
                .filter(SimpleMailMessage.class::isInstance)
                .map(SimpleMailMessage.class::cast)
                .toList();
    }

    protected List<SimpleMailMessage> sentMailTo(String email) {
        return sentMail().stream().filter(m -> m.getTo() != null && Arrays.asList(m.getTo()).contains(email)).toList();
    }

    protected static boolean isReminder(SimpleMailMessage message) {
        return message.getSubject() != null && message.getSubject().contains("Nhắc lịch");
    }

    protected static boolean isConfirmation(SimpleMailMessage message) {
        return message.getSubject() != null && message.getSubject().contains("Xác nhận lịch");
    }
}
