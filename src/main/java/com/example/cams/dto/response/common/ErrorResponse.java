package com.example.cams.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lỗi nghiệp vụ hoặc xác thực.")
public record ErrorResponse(
        @Schema(description = "Thông báo lỗi hiển thị cho người dùng.", example = "Vui lòng đăng nhập.") String error,
        @Schema(description = "Mã lỗi máy đọc được: validation_error, unauthorized, forbidden, forbidden_host, csrf_failed, "
                + "not_found, email_exists, invalid_credentials, rate_limited, invalid_transition, cutoff, "
                + "reschedule_limit, wrong_day, too_early, bad_request, internal_error.", example = "unauthorized") String code) {
}
