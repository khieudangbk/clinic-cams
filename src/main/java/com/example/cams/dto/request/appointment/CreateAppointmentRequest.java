package com.example.cams.dto.request.appointment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Yêu cầu đặt lịch. Bệnh nhân đặt cho chính mình; lễ tân phải cung cấp patient_name và "
        + "patient_phone (khách vãng lai) hoặc patient_email (bệnh nhân đã có tài khoản).")
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateAppointmentRequest(
        @JsonProperty("slot_id") @Schema(implementation = Long.class, description = "Id khung giờ còn trống (lấy từ /slots).", example = "101") Object slotId,
        @Schema(implementation = String.class, description = "Lý do khám, 1-500 ký tự.", example = "Khám tổng quát") Object reason,
        @JsonProperty("patient_name") @Schema(implementation = String.class, description = "Chỉ lễ tân: họ tên khách, 1-100 ký tự.", example = "Khách Vãng Lai") Object patientName,
        @JsonProperty("patient_phone") @Schema(implementation = String.class, description = "Chỉ lễ tân: điện thoại khách, 8-20 ký tự.", example = "0911222333") Object patientPhone,
        @JsonProperty("patient_email") @Schema(implementation = String.class, description = "Chỉ lễ tân: email của bệnh nhân đã có tài khoản.", example = "a@example.com") JsonNode patientEmail,
        @JsonProperty("booking_details") @Schema(implementation = BookingDetails.class) Object bookingDetails) {
}
