package com.example.cams.dto.request.schedule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Mẫu giờ làm việc tuần của bác sĩ.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record SaveScheduleRequest(
        @JsonProperty("doctor_id") @Schema(implementation = Long.class, description = "Id bác sĩ đang hoạt động.", example = "2") Object doctorId,
        @Schema(implementation = Integer[].class, description = "Danh sách ngày làm việc không rỗng, mỗi phần tử 0-5 (0=Thứ Hai ... 5=Thứ Bảy).",
                example = "[0,1,2,3,4]") Object weekdays,
        @JsonProperty("start_time") @Schema(implementation = String.class, description = "HH:mm, từ 07:00.", example = "08:00") Object startTime,
        @JsonProperty("end_time") @Schema(implementation = String.class, description = "HH:mm, sau start_time và không quá 17:00.", example = "16:00") Object endTime) {
}
