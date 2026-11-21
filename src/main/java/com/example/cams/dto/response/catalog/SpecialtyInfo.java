package com.example.cams.dto.response.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chuyên khoa.")
public record SpecialtyInfo(@Schema(example = "1") long id, @Schema(example = "Tai Mũi Họng") String name) {
}
