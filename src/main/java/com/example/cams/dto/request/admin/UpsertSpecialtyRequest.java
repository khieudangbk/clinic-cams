package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tạo mới hoặc đổi tên chuyên khoa.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpsertSpecialtyRequest(
        @Schema(description = "Bỏ trống để tạo mới; có giá trị để đổi tên chuyên khoa đó.", nullable = true) Long id,
        @Schema(description = "Tên chuyên khoa, 1-100 ký tự.", example = "Nhi khoa") String name) {
}
