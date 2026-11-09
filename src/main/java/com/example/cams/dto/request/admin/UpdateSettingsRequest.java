package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Cập nhật ít nhất một khóa: slot_minutes (5–120 phút), cutoff_hours (0–72 giờ), "
        + "max_reschedules (0–10 lần).",
        properties = {
                @StringToClassMapItem(key = "slot_minutes", value = Integer.class),
                @StringToClassMapItem(key = "cutoff_hours", value = Integer.class),
                @StringToClassMapItem(key = "max_reschedules", value = Integer.class)
        })
public record UpdateSettingsRequest(@Schema(hidden = true) Map<String, Object> values) {

    // Keep every supplied key so validation can reject unsupported settings.
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public UpdateSettingsRequest {
    }
}
