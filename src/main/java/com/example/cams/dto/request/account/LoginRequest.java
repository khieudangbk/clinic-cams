package com.example.cams.dto.request.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin đăng nhập.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
        @Schema(example = "staff@cams.local") String email,
        @Schema(example = "Demo@12345") String password) {
}
