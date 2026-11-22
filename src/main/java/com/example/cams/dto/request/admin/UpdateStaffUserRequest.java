package com.example.cams.dto.request.admin;

import com.example.cams.config.json.ActiveFlagDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@Schema(description = "Cập nhật tài khoản nhân sự; chỉ gửi các trường cần đổi.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStaffUserRequest {

    @Schema(description = "Họ tên mới, 1-100 ký tự.")
    private String name;

    @Schema(description = "Trạng thái hoạt động, chấp nhận boolean hoặc số 0/1.")
    private Boolean active;

    private Long specialtyId;

    @JsonIgnore
    @Schema(hidden = true)
    private boolean specialtyIdProvided;

    @JsonSetter(nulls = Nulls.FAIL)
    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = ActiveFlagDeserializer.class)
    public void setActive(Boolean active) {
        this.active = active;
    }

    @JsonSetter("specialty_id")
    @Schema(description = "Chuyên khoa mới; null để bỏ chuyên khoa.", nullable = true)
    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
        this.specialtyIdProvided = true;
    }
}
