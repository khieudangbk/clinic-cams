package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Tạo tài khoản nhân sự.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateStaffUserRequest(
        @Schema(implementation = String.class, allowableValues = { "doctor", "receptionist" }) Object role,
        @Schema(implementation = String.class, description = "Họ tên, 1-100 ký tự.", example = "BS. Lê D") Object name,
        @Schema(implementation = String.class, description = "Email hợp lệ, không trùng.", example = "d@cams.local") Object email,
        @Schema(implementation = String.class, description = "Mật khẩu 10-128 ký tự.", example = "Demo@12345") Object password,
        @JsonProperty("specialty_id") @Schema(implementation = Long.class, description = "Bắt buộc khi role = doctor.", example = "1") Object specialtyId) {
}
