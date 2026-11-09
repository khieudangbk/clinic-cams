package com.example.cams.dto.request.appointment;

import com.example.cams.config.json.NonNullTextDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "Yêu cầu đặt lịch. Bệnh nhân đặt cho chính mình; lễ tân phải cung cấp patient_name và "
        + "patient_phone (khách vãng lai) hoặc patient_email (bệnh nhân đã có tài khoản).")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateAppointmentRequest(
        @JsonProperty("slot_id") @Schema(description = "Id khung giờ còn trống (lấy từ /slots).", example = "101") Long slotId,
        @Schema(description = "Lý do khám, 1-500 ký tự.", example = "Khám tổng quát") String reason,
        @JsonProperty("patient_name") @Schema(description = "Chỉ lễ tân: họ tên khách, 1-100 ký tự.", example = "Khách Vãng Lai") String patientName,
        @JsonProperty("patient_phone") @Schema(description = "Chỉ lễ tân: điện thoại khách, 8-20 ký tự.", example = "0911222333") String patientPhone,
        @JsonProperty("patient_email") @Schema(description = "Chỉ lễ tân: email của bệnh nhân đã có tài khoản.", example = "a@example.com") @JsonDeserialize(using = NonNullTextDeserializer.class) String patientEmail,
        @JsonProperty("booking_details") BookingDetails bookingDetails) {
}
