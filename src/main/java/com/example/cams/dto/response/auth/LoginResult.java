package com.example.cams.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Kết quả nội bộ của đăng nhập: body trả về cùng refresh token đặt vào cookie (không được trả trong body).")
public record LoginResult(AuthResponse authResponse, String refreshToken) {
}
