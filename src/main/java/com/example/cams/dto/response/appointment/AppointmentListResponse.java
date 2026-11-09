package com.example.cams.dto.response.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh sách lịch hẹn theo vai trò: bệnh nhân thấy của mình, bác sĩ thấy của mình, lễ tân/quản lý thấy tất cả.")
public record AppointmentListResponse(List<AppointmentInfo> appointments) {
}
