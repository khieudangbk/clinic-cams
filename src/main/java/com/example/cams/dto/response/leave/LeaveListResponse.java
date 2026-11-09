package com.example.cams.dto.response.leave;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh sách lượt nghỉ: bác sĩ thấy của mình, quản lý thấy tất cả.")
public record LeaveListResponse(List<LeaveInfo> leaves) {
}
