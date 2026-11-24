package com.example.cams.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh sách bản ghi gửi email.")
public record EmailLogResponse(List<EmailLogEntry> logs) {
}
