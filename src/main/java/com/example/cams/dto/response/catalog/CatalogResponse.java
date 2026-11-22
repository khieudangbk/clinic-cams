package com.example.cams.dto.response.catalog;

import com.example.cams.dto.response.schedule.ScheduleInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh mục đầy đủ dành cho người đã đăng nhập, kèm mẫu giờ làm việc.")
public record CatalogResponse(
        List<SpecialtyInfo> specialties,
        List<DoctorInfo> doctors,
        List<ScheduleInfo> schedules) {
}
