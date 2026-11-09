package com.example.cams.dto.request.leave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Đăng ký nghỉ của bác sĩ.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateLeaveRequest(
        @JsonProperty("doctor_id") @Schema(description = "Quản lý bắt buộc chỉ định; bác sĩ tự đăng ký thì bị bỏ qua.", example = "2") Long doctorId,
        @JsonProperty("starts_at") @Schema(description = "ISO-8601 có offset.", example = "2026-11-25T08:00:00+07:00") String startsAt,
        @JsonProperty("ends_at") @Schema(description = "ISO-8601 có offset, sau starts_at.", example = "2026-11-25T12:00:00+07:00") String endsAt,
        @Schema(description = "Lý do nghỉ, 1-300 ký tự.", example = "Nghỉ ốm") String reason) {
}
