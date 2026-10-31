package com.example.cams.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Thông tin đăng nhập module JWT.")
public record LoginRequest(
                @Schema(description = "Email đăng nhập.", example = "user@example.com") @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email,

                @Schema(description = "Mật khẩu.", example = "Str0ng@Pass") @NotBlank(message = "Password is required.") String password) {
}
