package com.example.cams.dto.response.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bác sĩ đang hoạt động.")
public record DoctorInfo(
        @Schema(example = "2") long id,
        @Schema(example = "BS. Trần B") String name,
        @JsonProperty("specialty_id") @Schema(example = "1") Long specialtyId,
        @JsonProperty("specialty_name") @Schema(example = "Tai Mũi Họng") String specialtyName) {
}
