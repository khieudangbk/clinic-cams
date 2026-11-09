package com.example.cams.dto.response.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh mục công khai.")
public record PublicCatalogResponse(
        List<SpecialtyInfo> specialties,
        List<DoctorInfo> doctors,
        @Schema(example = "2026-11-23T08:00+07:00") String now,
        @JsonProperty("clock_mode") @Schema(allowableValues = { "demo", "real" }) String clockMode) {
}
