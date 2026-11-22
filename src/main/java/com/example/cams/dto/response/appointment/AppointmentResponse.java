package com.example.cams.dto.response.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Một lịch hẹn.")
public record AppointmentResponse(AppointmentInfo appointment) {
}
