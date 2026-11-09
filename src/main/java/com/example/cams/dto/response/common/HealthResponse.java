package com.example.cams.dto.response.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tình trạng dịch vụ.")
public record HealthResponse(
        @Schema(example = "true") boolean ok,
        @Schema(example = "CAMS") String application,
        @JsonProperty("clock_mode") @Schema(description = "demo = đồng hồ cố định (cams.demo-now), real = giờ thật.",
                allowableValues = { "demo", "real" }) String clockMode) {
}
