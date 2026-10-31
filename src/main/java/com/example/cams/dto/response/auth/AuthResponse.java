package com.example.cams.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả đăng nhập/làm mới token.")
public class AuthResponse {
    @Schema(description = "JWT access token, gửi trong header Authorization: Bearer.")
    private String accessToken;
    @Schema(description = "Người dùng đã xác thực.")
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin người dùng của module JWT.")
    public static class UserInfo {
        @Schema(description = "Id người dùng.", example = "1")
        Long id;
        @Schema(description = "Họ tên.", example = "Nguyen Van A")
        String name;
        @Schema(description = "Email.", example = "user@example.com")
        String email;
        @Schema(description = "Vai trò.", example = "USER")
        String role;
        @Schema(description = "Trạng thái tài khoản.", example = "ACTIVE")
        String status;
    }
}
