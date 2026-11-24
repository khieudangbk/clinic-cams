package com.example.cams.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả một lần chạy tác vụ nhắc lịch.")
public record ReminderRunResponse(
        @Schema(description = "Số email nhắc đã gửi thành công.", example = "1") int sent,
        @Schema(description = "Số lịch được xử lý nhưng không gửi (không có email, email bị tắt hoặc gửi lỗi).", example = "0") int skipped) {
}
