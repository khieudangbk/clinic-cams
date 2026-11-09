package com.example.cams.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mẫu giờ làm việc tuần của một bác sĩ.")
public record ScheduleInfo(
        @JsonProperty("doctor_id") long doctorId,
        @Schema(description = "Các ngày làm việc, định dạng CSV: 0=Thứ Hai ... 5=Thứ Bảy.", example = "0,1,2,3,4") String weekdays,
        @JsonProperty("start_time") @Schema(example = "08:00") String startTime,
        @JsonProperty("end_time") @Schema(example = "16:00") String endTime) {
}
