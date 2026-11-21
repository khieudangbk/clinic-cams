package com.example.cams.dto.response.appointment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một dòng nhật ký thao tác trên lịch hẹn.")
public record AuditEntry(
        long id,
        @JsonProperty("appointment_id") long appointmentId,
        @JsonProperty("actor_id") Long actorId,
        @JsonProperty("actor_name") String actorName,
        @Schema(example = "Chuyển trạng thái confirmed") String action,
        @JsonProperty("before_json") @Schema(description = "Trạng thái trước (JSON dạng chuỗi).") String beforeJson,
        @JsonProperty("after_json") @Schema(description = "Trạng thái sau (JSON dạng chuỗi).") String afterJson,
        String reason,
        @JsonProperty("created_at") String createdAt) {
}
