package com.example.cams.dto.request.appointment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

/** Retains raw JSON values for validation in the service. */
@Schema(description = "Dữ liệu cho thao tác chuyển trạng thái; trường cần dùng tùy theo action.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransitionRequest(
        @Schema(implementation = String.class, description = "cancel/reschedule: bắt buộc với bệnh nhân (tối đa 300 ký tự). reject: bắt buộc. "
                + "Các action khác: bỏ qua.", example = "Khách đổi ý") JsonNode reason,
        @JsonProperty("slot_id") @Schema(implementation = Long.class, description = "Chỉ reschedule: khung giờ mới, phải khác khung giờ hiện tại.", example = "205") Object slotId) {
}
