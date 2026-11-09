package com.example.cams.dto.response.account;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Phiên đăng nhập hiện tại.")
public record SessionResponse(
        UserInfo user,
        @Schema(description = "CSRF token của phiên.") String csrf,
        @Schema(description = "Cấu hình hệ thống: slot_minutes, cutoff_hours, max_reschedules, max_active...",
                example = "{\"slot_minutes\":30,\"cutoff_hours\":2,\"max_reschedules\":2}") Map<String, Integer> settings,
        @Schema(description = "Thời gian hiện tại của hệ thống (+07:00).", example = "2026-11-23T08:00+07:00") String now) {
}
