package com.example.cams.dto.request.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Đăng ký tài khoản bệnh nhân.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @Schema(implementation = String.class, description = "Họ tên, 1-100 ký tự.", example = "Nguyễn Văn A") Object name,
        @Schema(implementation = String.class, description = "Email hợp lệ, tối đa 150 ký tự, không được trùng.", example = "a@example.com") Object email,
        @Schema(implementation = String.class, description = "Mật khẩu 10-128 ký tự.", example = "Testing@12345") Object password,
        @Schema(implementation = String.class, description = "Số điện thoại 8-20 ký tự gồm số, +, khoảng trắng, ngoặc, gạch ngang (tùy chọn).",
                example = "0911222333") JsonNode phone) {
}
