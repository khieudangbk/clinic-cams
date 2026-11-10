package com.example.cams.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Auth, CSRF, origin-guard, validation and booking-rule behaviour of the CAMS API over the full Spring context. */
@SpringBootTest
@AutoConfigureMockMvc
class CamsApiIntegrationTest {

    private static final String HOST = "127.0.0.1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private record Session(String csrf, Cookie cookie) {
    }

    private Session login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password))
                        .header("Host", HOST))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Session(json.get("csrf").asText(), result.getResponse().getCookie("cams_session"));
    }

    private Session registerAndLogin() throws Exception {
        String email = "it-" + System.nanoTime() + "@example.com";
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"IT Patient\",\"email\":\"%s\",\"password\":\"Testing@12345\"}"
                                .formatted(email))
                        .header("Host", HOST))
                .andExpect(status().isCreated());
        return login(email, "Testing@12345");
    }

    private long firstFreeSlot(int daysAhead) throws Exception {
        MvcResult catalog = mockMvc.perform(get("/api/public/catalog").header("Host", HOST))
                .andExpect(status().isOk()).andReturn();
        long doctorId = objectMapper.readTree(catalog.getResponse().getContentAsString())
                .get("doctors").get(0).get("id").asLong();
        String day = LocalDate.now().plusDays(daysAhead).format(DateTimeFormatter.ISO_LOCAL_DATE);
        MvcResult slots = mockMvc.perform(get("/api/public/slots").param("doctor_id", String.valueOf(doctorId))
                        .param("day", day).header("Host", HOST))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(slots.getResponse().getContentAsString()).get("slots").get(0).get("id").asLong();
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health").header("Host", HOST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.application").value("CAMS"));
    }

    @Test
    void foreignHostIsForbidden() throws Exception {
        mockMvc.perform(get("/api/health").header("Host", "evil.example.com"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden_host"));
    }

    @Test
    void foreignOriginOnPostIsForbidden() throws Exception {
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.c\",\"password\":\"x\"}")
                        .header("Host", HOST).header("Origin", "http://evil.example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = "dup-" + System.nanoTime() + "@example.com";
        String body = "{\"name\":\"Dup\",\"email\":\"%s\",\"password\":\"Testing@12345\"}".formatted(email);
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Host", HOST)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email_exists"));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"staff@cams.local\",\"password\":\"Wrong@12345\"}")
                        .header("Host", HOST))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void repeatedFailedLoginsAreRateLimited() throws Exception {
        String body = "{\"email\":\"ratelimit-%d@example.com\",\"password\":\"Wrong@12345\"}"
                .formatted(System.nanoTime());
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(body)
                    .header("Host", HOST)).andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("rate_limited"));
    }

    @Test
    void malformedJsonIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content("{not json")
                        .header("Host", HOST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("bad_request"));
    }

    @Test
    void protectedEndpointsRequireSession() throws Exception {
        mockMvc.perform(get("/api/session").header("Host", HOST))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
        mockMvc.perform(get("/api/appointments").header("Host", HOST))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessionEndpointReturnsCurrentUser() throws Exception {
        Session s = registerAndLogin();
        mockMvc.perform(get("/api/session").header("Host", HOST).cookie(s.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("patient"))
                .andExpect(jsonPath("$.csrf").value(s.csrf()));
    }

    @Test
    void mutatingRequestWithoutCsrfIsForbidden() throws Exception {
        Session s = registerAndLogin();
        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slot_id\":1,\"reason\":\"x\"}").header("Host", HOST).cookie(s.cookie()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("csrf_failed"));
    }

    @Test
    void unknownAppointmentActionIsNotFound() throws Exception {
        Session s = registerAndLogin();
        mockMvc.perform(post("/api/appointments/1/explode").contentType(MediaType.APPLICATION_JSON)
                        .content("{}").header("Host", HOST).header("X-CSRF-Token", s.csrf()).cookie(s.cookie()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found"));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        Session s = registerAndLogin();
        mockMvc.perform(post("/api/logout").contentType(MediaType.APPLICATION_JSON).content("{}")
                        .header("Host", HOST).header("X-CSRF-Token", s.csrf()).cookie(s.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        mockMvc.perform(get("/api/session").header("Host", HOST).cookie(s.cookie()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotBookSameSlotTwice() throws Exception {
        Session first = registerAndLogin();
        Session second = registerAndLogin();
        long slotId = firstFreeSlot(4);
        String body = "{\"slot_id\":%d,\"reason\":\"Kham\"}".formatted(slotId);

        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST).header("X-CSRF-Token", first.csrf()).cookie(first.cookie()))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON).content(body)
                        .header("Host", HOST).header("X-CSRF-Token", second.csrf()).cookie(second.cookie()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void patientCannotAccessAdminUserList() throws Exception {
        Session s = registerAndLogin();
        mockMvc.perform(get("/api/users").header("Host", HOST).cookie(s.cookie()))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicSlotsRejectInvalidParameters() throws Exception {
        mockMvc.perform(get("/api/public/slots").param("doctor_id", "abc").param("day", "2026-01-01")
                        .header("Host", HOST))
                .andExpect(status().isBadRequest());
    }
}
