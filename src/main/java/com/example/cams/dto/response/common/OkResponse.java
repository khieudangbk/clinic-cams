package com.example.cams.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả thao tác không trả dữ liệu.")
public record OkResponse(@Schema(example = "true") boolean ok) {
}
