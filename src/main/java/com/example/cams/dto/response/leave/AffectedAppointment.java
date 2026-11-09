package com.example.cams.dto.response.leave;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lịch hẹn bị ảnh hưởng bởi lượt nghỉ.")
public record AffectedAppointment(
        long id,
        String status,
        @JsonProperty("starts_at") String startsAt,
        @JsonProperty("patient_name") String patientName,
        @JsonProperty("patient_phone") String patientPhone) {
}
