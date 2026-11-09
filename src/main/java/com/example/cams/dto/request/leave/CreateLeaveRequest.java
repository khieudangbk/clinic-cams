package com.example.cams.dto.request.leave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Đăng ký nghỉ của bác sĩ.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateLeaveRequest(
        @JsonProperty("doctor_id") @Schema(implementation = Long.class, description = "Quản lý bắt buộc chỉ định; bác sĩ tự đăng ký thì bị bỏ qua.", example = "2") Object doctorId,
        @JsonProperty("starts_at") @Schema(implementation = String.class, description = "ISO-8601 có offset.", example = "2026-11-25T08:00:00+07:00") Object startsAt,
        @JsonProperty("ends_at") @Schema(implementation = String.class, description = "ISO-8601 có offset, sau starts_at.", example = "2026-11-25T12:00:00+07:00") Object endsAt,
        @Schema(implementation = String.class, description = "Lý do nghỉ, 1-300 ký tự.", example = "Nghỉ ốm") Object reason) {
}
