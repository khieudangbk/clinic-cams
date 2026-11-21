package com.example.cams.dto.request.appointment;

import com.example.cams.config.json.NonNullTextDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin người khám (tùy chọn). Chỉ chấp nhận đúng các trường dưới đây; mỗi trường là chuỗi "
        + "tối đa 100 ký tự (address và extra_note tối đa 500).")
public record BookingDetails(
        @JsonProperty("booking_for") @Schema(description = "Đặt cho ai. Nếu có, bắt buộc kèm name và phone.",
                allowableValues = { "self", "relative" }) @JsonDeserialize(using = NonNullTextDeserializer.class) String bookingFor,
        @Schema(description = "Họ tên người khám.", example = "Nguyễn Thị C") @JsonDeserialize(using = NonNullTextDeserializer.class) String name,
        @Schema(description = "Điện thoại người khám, 8-20 ký tự.", example = "0988777666") @JsonDeserialize(using = NonNullTextDeserializer.class) String phone,
        @JsonProperty("birth_year") @Schema(description = "Năm sinh từ 1900 đến năm hiện tại.", example = "1990") @JsonDeserialize(using = NonNullTextDeserializer.class) String birthYear,
        @Schema(allowableValues = { "", "male", "female", "other" }) @JsonDeserialize(using = NonNullTextDeserializer.class) String gender,
        @Schema(example = "Hà Nội") @JsonDeserialize(using = NonNullTextDeserializer.class) String province,
        @Schema(example = "Phường Cầu Giấy") @JsonDeserialize(using = NonNullTextDeserializer.class) String ward,
        @Schema(example = "12 Xuân Thủy") @JsonDeserialize(using = NonNullTextDeserializer.class) String address,
        @JsonProperty("extra_note") @Schema(example = "Ho kéo dài 3 ngày") @JsonDeserialize(using = NonNullTextDeserializer.class) String extraNote,
        @Schema(description = "Hiện chỉ hỗ trợ thanh toán tại phòng khám.", allowableValues = { "at_clinic" }) @JsonDeserialize(using = NonNullTextDeserializer.class) String payment) {

    @JsonAnySetter
    public void rejectUnknownField(String field, JsonNode value) {
        throw new IllegalArgumentException("Trường thông tin người khám không được hỗ trợ: " + field);
    }
}
