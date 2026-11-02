package com.example.cams.dto.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Documentation-only request/response models for the CAMS booking API. The controllers exchange
 * plain JSON maps at runtime (so field validation lives in the services); these records exist
 * so Swagger can describe the exact shape of every payload.
 */
public final class CamsApiModels {

    private CamsApiModels() {
    }

    // ---------------------------------------------------------------- common




    // ---------------------------------------------------------------- users & session







    @Schema(description = "Tình trạng dịch vụ.")
    public record HealthResponse(
            @Schema(example = "true") boolean ok,
            @Schema(example = "CAMS") String application,
            @JsonProperty("clock_mode") @Schema(description = "demo = đồng hồ cố định (cams.demo-now), real = giờ thật.",
                    allowableValues = { "demo", "real" }) String clockMode) {
    }

    // ---------------------------------------------------------------- catalog & slots








    // ---------------------------------------------------------------- appointments









    // ---------------------------------------------------------------- leaves






    // ---------------------------------------------------------------- schedules & admin







}
