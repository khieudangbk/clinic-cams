package com.example.cams.dto.request.appointment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin người khám (tùy chọn). Chỉ chấp nhận đúng các trường dưới đây; mỗi trường là chuỗi "
        + "tối đa 100 ký tự (address và extra_note tối đa 500).")
public record BookingDetails(
        @JsonProperty("booking_for") @Schema(description = "Đặt cho ai. Nếu có, bắt buộc kèm name và phone.",
                allowableValues = { "self", "relative" }) String bookingFor,
        @Schema(description = "Họ tên người khám.", example = "Nguyễn Thị C") String name,
        @Schema(description = "Điện thoại người khám, 8-20 ký tự.", example = "0988777666") String phone,
        @JsonProperty("birth_year") @Schema(description = "Năm sinh từ 1900 đến năm hiện tại.", example = "1990") String birthYear,
        @Schema(allowableValues = { "", "male", "female", "other" }) String gender,
        @Schema(example = "Hà Nội") String province,
        @Schema(example = "Phường Cầu Giấy") String ward,
        @Schema(example = "12 Xuân Thủy") String address,
        @JsonProperty("extra_note") @Schema(example = "Ho kéo dài 3 ngày") String extraNote,
        @Schema(description = "Hiện chỉ hỗ trợ thanh toán tại phòng khám.", allowableValues = { "at_clinic" }) String payment) {
}
