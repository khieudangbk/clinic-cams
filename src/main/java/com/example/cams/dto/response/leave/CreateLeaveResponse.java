package com.example.cams.dto.response.leave;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Kết quả đăng ký nghỉ.")
public record CreateLeaveResponse(
        long id,
        List<AffectedAppointment> affected,
        @Schema(example = "Đã chặn đặt mới. Lễ tân cần liên hệ các lịch bị ảnh hưởng.") String message) {
}
