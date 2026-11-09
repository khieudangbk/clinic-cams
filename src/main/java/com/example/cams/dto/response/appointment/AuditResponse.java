package com.example.cams.dto.response.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Lịch sử thao tác của một lịch hẹn.")
public record AuditResponse(List<AuditEntry> audit) {
}
