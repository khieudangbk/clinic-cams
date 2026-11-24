package com.example.cams.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một lần gửi email (xác nhận hoặc nhắc lịch).")
public record EmailLogEntry(
        long id,
        @JsonProperty("appointment_id") long appointmentId,
        @Schema(description = "confirm hoặc reminder", example = "confirm") String kind,
        @Schema(description = "Email người nhận; rỗng nếu không có.") String recipient,
        @Schema(description = "sent, skipped, failed hoặc disabled", example = "sent") String status,
        @Schema(description = "Thông báo lỗi khi status = failed.") String error,
        @JsonProperty("created_at") String createdAt) {
}
