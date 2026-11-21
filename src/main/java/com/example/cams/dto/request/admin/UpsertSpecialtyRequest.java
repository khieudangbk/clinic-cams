package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Tạo mới hoặc đổi tên chuyên khoa.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpsertSpecialtyRequest(
        @Schema(implementation = Long.class, description = "Bỏ trống để tạo mới; có giá trị để đổi tên chuyên khoa đó.", nullable = true) Object id,
        @Schema(implementation = String.class, description = "Tên chuyên khoa, 1-100 ký tự.", example = "Nhi khoa") Object name) {
}
