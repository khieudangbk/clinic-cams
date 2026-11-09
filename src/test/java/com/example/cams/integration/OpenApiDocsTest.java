package com.example.cams.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The OpenAPI document is generated and documents the CAMS endpoints, their payloads and security. */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsDescribeEndpointsAndModels() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("CAMS - Clinic Appointment Management System API"))
                .andExpect(jsonPath("$.paths['/api/appointments'].post.summary").exists())
                .andExpect(jsonPath("$.paths['/api/appointments'].post.security").isArray())
                .andExpect(jsonPath("$.paths['/api/login'].post.responses['429']").exists())
                .andExpect(jsonPath("$.paths['/api/appointments'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.CreateAppointmentRequest.properties.slot_id").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.code").exists())
                .andExpect(jsonPath("$.components.securitySchemes.camsSession").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerJwt").doesNotExist());
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
