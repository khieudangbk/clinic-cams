package com.example.cams.dto.response.slot;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh sách khung giờ còn trống trong ngày.")
public record SlotsResponse(List<SlotInfo> slots) {
}
