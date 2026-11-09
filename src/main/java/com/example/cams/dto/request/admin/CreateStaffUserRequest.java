package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tạo tài khoản nhân sự.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateStaffUserRequest(
        @Schema(allowableValues = { "doctor", "receptionist" }) String role,
        @Schema(description = "Họ tên, 1-100 ký tự.", example = "BS. Lê D") String name,
        @Schema(description = "Email hợp lệ, không trùng.", example = "d@cams.local") String email,
        @Schema(description = "Mật khẩu 10-128 ký tự.", example = "Demo@12345") String password,
        @JsonProperty("specialty_id") @Schema(description = "Bắt buộc khi role = doctor.", example = "1") Long specialtyId) {
}
