package com.example.cams.dto.response.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả lưu mẫu tuần.")
public record SaveScheduleResponse(
        @Schema(example = "true") boolean ok,
        @Schema(example = "Đã lưu mẫu tuần. Lịch hẹn đã tồn tại được giữ nguyên.") String message) {
}
