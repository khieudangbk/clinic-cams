package com.example.cams.dto.response.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin công khai của một tài khoản.")
public record UserInfo(
        @Schema(example = "3") long id,
        @Schema(example = "Nguyễn Văn A") String name,
        @Schema(example = "patient@example.com") String email,
        @Schema(example = "0911222333", nullable = true) String phone,
        @Schema(description = "Vai trò.", allowableValues = { "patient", "receptionist", "doctor", "manager" },
                example = "patient") String role,
        @JsonProperty("specialty_id") @Schema(description = "Chuyên khoa (chỉ bác sĩ).", nullable = true) Long specialtyId,
        @Schema(description = "1 = đang hoạt động, 0 = đã khóa.", allowableValues = { "0", "1" }, example = "1") int active) {
}
