package com.example.cams.dto.response.admin;

import com.example.cams.dto.response.account.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Danh sách nhân sự (bác sĩ, lễ tân, quản lý).")
public record UserListResponse(List<UserInfo> users) {
}
