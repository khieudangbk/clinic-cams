package com.example.cams.dto.response.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chuyên khoa.")
public record SpecialtyInfo(@Schema(example = "1") long id, @Schema(example = "Tai Mũi Họng") String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Chỉ có trong /api/catalog của quản lý: 1 đang hoạt động, 0 đã ngừng.", example = "1") Integer active) {
}
