package com.example.cams.dto.request.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Thông tin đăng nhập.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
        @Schema(implementation = String.class, example = "staff@cams.local") Object email,
        @Schema(implementation = String.class, example = "Demo@12345") Object password) {
}
