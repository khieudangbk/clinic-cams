package com.example.cams.dto.response.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một lượt nghỉ của bác sĩ.")
public record LeaveInfo(
        long id,
        @JsonProperty("doctor_id") long doctorId,
        @JsonProperty("doctor_name") String doctorName,
        @JsonProperty("starts_at") String startsAt,
        @JsonProperty("ends_at") String endsAt,
        String reason,
        @JsonProperty("created_by") Long createdBy) {
}
