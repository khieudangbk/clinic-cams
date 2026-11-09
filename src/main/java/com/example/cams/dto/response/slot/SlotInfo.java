package com.example.cams.dto.response.slot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Khung giờ còn trống.")
public record SlotInfo(
        @Schema(example = "101") long id,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonProperty("doctor_id") @Schema(description = "Chỉ có ở /api/slots.", example = "2") Long doctorId,
        @JsonProperty("starts_at") @Schema(example = "2026-11-24T08:00+07:00") String startsAt,
        @JsonProperty("ends_at") @Schema(example = "2026-11-24T08:30+07:00") String endsAt) {
}
