package com.example.cams.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả tạo mới, trả về id.")
public record CreatedIdResponse(@Schema(description = "Id bản ghi vừa tạo.", example = "12") long id) {
}
