package com.example.cams.dto.request.account;

import com.example.cams.config.json.NonNullTextDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "Đăng ký tài khoản bệnh nhân.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @Schema(description = "Họ tên, 1-100 ký tự.", example = "Nguyễn Văn A") String name,
        @Schema(description = "Email hợp lệ, tối đa 150 ký tự, không được trùng.", example = "a@example.com") String email,
        @Schema(description = "Mật khẩu 10-128 ký tự.", example = "Testing@12345") String password,
        @Schema(description = "Số điện thoại 8-20 ký tự gồm số, +, khoảng trắng, ngoặc, gạch ngang (tùy chọn).",
                example = "0911222333") @JsonDeserialize(using = NonNullTextDeserializer.class) String phone) {
}
