package com.example.cams.dto.response.appointment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một lịch hẹn.")
public record AppointmentInfo(
        @Schema(example = "7") long id,
        @JsonProperty("patient_id") @Schema(nullable = true) Long patientId,
        @JsonProperty("slot_id") long slotId,
        @JsonProperty("doctor_id") Long doctorId,
        @JsonProperty("starts_at") @Schema(example = "2026-11-24T08:00+07:00") String startsAt,
        @JsonProperty("ends_at") @Schema(example = "2026-11-24T08:30+07:00") String endsAt,
        @JsonProperty("doctor_name") String doctorName,
        @JsonProperty("patient_name") String patientName,
        @JsonProperty("patient_phone") String patientPhone,
        @Schema(allowableValues = { "pending", "confirmed", "arrived", "completed", "cancelled", "noshow" }) String status,
        @Schema(description = "Kênh đặt: online (bệnh nhân) hoặc staff (lễ tân).", allowableValues = { "online", "staff" }) String channel,
        String reason,
        @JsonProperty("reschedule_count") @Schema(description = "Số lần đã đổi lịch.") int rescheduleCount,
        @JsonProperty("created_by") Long createdBy,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("doctor_on_leave") @Schema(description = "1 nếu bác sĩ đang nghỉ trong khung giờ này.", allowableValues = { "0", "1" }) int doctorOnLeave,
        @JsonProperty("booking_details") @Schema(description = "JSON thông tin người khám đã lưu.") String bookingDetails) {
}
