package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

@Schema(description = "Cập nhật tài khoản nhân sự; chỉ gửi các trường cần đổi.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateStaffUserRequest(
        @Schema(implementation = String.class, description = "Họ tên mới, 1-100 ký tự.") JsonNode name,
        @Schema(implementation = Integer.class, description = "1 = hoạt động, 0 = khóa.", allowableValues = { "0", "1" }) JsonNode active,
        @JsonProperty("specialty_id") @Schema(implementation = Long.class, description = "Chuyên khoa mới; null để bỏ chuyên khoa.", nullable = true) JsonNode specialtyId) {
}
