package com.example.cams.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import com.example.cams.util.validator.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Đăng ký tài khoản module JWT.")
public record RegisterRequest(
                @Schema(description = "Họ tên, tối đa 100 ký tự.", example = "Nguyen Van A") @NotBlank(message = "Name is required.") @Size(max = 100, message = "Name must not exceed 100 characters.") String name,

                @Schema(description = "Email duy nhất, tối đa 150 ký tự.", example = "user@example.com") @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") @Size(max = 150, message = "Email must not exceed 150 characters.") String email,

                @Schema(description = "Mật khẩu 8-100 ký tự, đủ mạnh (chữ hoa, chữ thường, số, ký tự đặc biệt).", example = "Str0ng@Pass") @NotBlank(message = "Password is required.") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.") @StrongPassword String password) {
}
