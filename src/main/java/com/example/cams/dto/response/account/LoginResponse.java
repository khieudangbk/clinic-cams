package com.example.cams.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả đăng nhập. Cookie `cams_session` được đặt qua header Set-Cookie.")
public record LoginResponse(
        UserInfo user,
        @Schema(description = "CSRF token cần gửi trong header X-CSRF-Token cho mọi request ghi.") String csrf) {
}
