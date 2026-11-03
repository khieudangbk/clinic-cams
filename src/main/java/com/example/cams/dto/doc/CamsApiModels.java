package com.example.cams.dto.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Documentation-only request/response models for the CAMS booking API. The controllers exchange
 * plain JSON maps at runtime (so field validation lives in the services); these records exist
 * so Swagger can describe the exact shape of every payload.
 */
public final class CamsApiModels {

    private CamsApiModels() {
    }

    // ---------------------------------------------------------------- common

    @Schema(description = "Lỗi nghiệp vụ hoặc xác thực.")
    public record ErrorResponse(
            @Schema(description = "Thông báo lỗi hiển thị cho người dùng.", example = "Vui lòng đăng nhập.") String error,
            @Schema(description = "Mã lỗi máy đọc được: validation_error, unauthorized, forbidden, forbidden_host, csrf_failed, "
                    + "not_found, email_exists, invalid_credentials, rate_limited, invalid_transition, cutoff, "
                    + "reschedule_limit, wrong_day, too_early, bad_request, internal_error.", example = "unauthorized") String code) {
    }



    // ---------------------------------------------------------------- users & session

    @Schema(description = "Thông tin công khai của một tài khoản.")
    public record UserInfo(
            @Schema(example = "3") long id,
            @Schema(example = "Nguyễn Văn A") String name,
            @Schema(example = "patient@example.com") String email,
            @Schema(example = "0911222333", nullable = true) String phone,
            @Schema(description = "Vai trò.", allowableValues = { "patient", "receptionist", "doctor", "manager" },
                    example = "patient") String role,
            @JsonProperty("specialty_id") @Schema(description = "Chuyên khoa (chỉ bác sĩ).", nullable = true) Long specialtyId,
            @Schema(description = "1 = đang hoạt động, 0 = đã khóa.", allowableValues = { "0", "1" }, example = "1") int active) {
    }

    @Schema(description = "Đăng ký tài khoản bệnh nhân.")
    public record RegisterRequest(
            @Schema(description = "Họ tên, 1-100 ký tự.", example = "Nguyễn Văn A") String name,
            @Schema(description = "Email hợp lệ, tối đa 150 ký tự, không được trùng.", example = "a@example.com") String email,
            @Schema(description = "Mật khẩu 10-128 ký tự.", example = "Testing@12345") String password,
            @Schema(description = "Số điện thoại 8-20 ký tự gồm số, +, khoảng trắng, ngoặc, gạch ngang (tùy chọn).",
                    example = "0911222333") String phone) {
    }

    @Schema(description = "Kết quả đăng ký.")
    public record RegisterResponse(
            @Schema(description = "Id tài khoản mới.", example = "15") long id,
            @Schema(example = "Đăng ký thành công. Bạn có thể đăng nhập.") String message) {
    }

    @Schema(description = "Thông tin đăng nhập.")
    public record LoginRequest(
            @Schema(example = "staff@cams.local") String email,
            @Schema(example = "Demo@12345") String password) {
    }

    @Schema(description = "Kết quả đăng nhập. Cookie `cams_session` được đặt qua header Set-Cookie.")
    public record LoginResponse(
            UserInfo user,
            @Schema(description = "CSRF token cần gửi trong header X-CSRF-Token cho mọi request ghi.") String csrf) {
    }


    @Schema(description = "Tình trạng dịch vụ.")
    public record HealthResponse(
            @Schema(example = "true") boolean ok,
            @Schema(example = "CAMS") String application,
            @JsonProperty("clock_mode") @Schema(description = "demo = đồng hồ cố định (cams.demo-now), real = giờ thật.",
                    allowableValues = { "demo", "real" }) String clockMode) {
    }

    // ---------------------------------------------------------------- catalog & slots








    // ---------------------------------------------------------------- appointments









    // ---------------------------------------------------------------- leaves






    // ---------------------------------------------------------------- schedules & admin







}
