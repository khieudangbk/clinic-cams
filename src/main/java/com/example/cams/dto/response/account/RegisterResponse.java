package com.example.cams.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả đăng ký.")
public record RegisterResponse(
        @Schema(description = "Id tài khoản mới.", example = "15") long id,
        @Schema(example = "Đăng ký thành công. Bạn có thể đăng nhập.") String message) {
}
