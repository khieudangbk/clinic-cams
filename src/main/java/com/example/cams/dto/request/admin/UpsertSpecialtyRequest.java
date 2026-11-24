package com.example.cams.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.cams.config.json.ActiveFlagDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "Tạo mới, đổi tên hoặc ngừng/mở lại chuyên khoa.")
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpsertSpecialtyRequest(
        @Schema(description = "Bỏ trống để tạo mới; có giá trị để sửa chuyên khoa đó.", nullable = true) Long id,
        @Schema(description = "Tên chuyên khoa, 1-100 ký tự. Bắt buộc khi tạo mới; khi sửa mà bỏ trống thì giữ tên cũ.", example = "Nhi khoa", nullable = true) String name,
        @JsonDeserialize(using = ActiveFlagDeserializer.class)
        @Schema(description = "Chỉ khi sửa (có id): false/0 ngừng chuyên khoa (ẩn khỏi tra cứu cùng các bác sĩ của nó, không đặt được), true/1 mở lại. Bỏ trống thì giữ nguyên.", nullable = true) Boolean active) {
}
