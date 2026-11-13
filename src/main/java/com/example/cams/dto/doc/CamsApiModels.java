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

    @Schema(description = "Kết quả thao tác không trả dữ liệu.")
    public record OkResponse(@Schema(example = "true") boolean ok) {
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

    @Schema(description = "Phiên đăng nhập hiện tại.")
    public record SessionResponse(
            UserInfo user,
            @Schema(description = "CSRF token của phiên.") String csrf,
            @Schema(description = "Cấu hình hệ thống: slot_minutes, cutoff_hours, max_reschedules, max_active...",
                    example = "{\"slot_minutes\":30,\"cutoff_hours\":2,\"max_reschedules\":2}") Map<String, Integer> settings,
            @Schema(description = "Thời gian hiện tại của hệ thống (+07:00).", example = "2026-11-23T08:00+07:00") String now) {
    }

    @Schema(description = "Tình trạng dịch vụ.")
    public record HealthResponse(
            @Schema(example = "true") boolean ok,
            @Schema(example = "CAMS") String application,
            @JsonProperty("clock_mode") @Schema(description = "demo = đồng hồ cố định (cams.demo-now), real = giờ thật.",
                    allowableValues = { "demo", "real" }) String clockMode) {
    }

    // ---------------------------------------------------------------- catalog & slots

    @Schema(description = "Chuyên khoa.")
    public record SpecialtyInfo(@Schema(example = "1") long id, @Schema(example = "Tai Mũi Họng") String name) {
    }

    @Schema(description = "Bác sĩ đang hoạt động.")
    public record DoctorInfo(
            @Schema(example = "2") long id,
            @Schema(example = "BS. Trần B") String name,
            @JsonProperty("specialty_id") @Schema(example = "1") Long specialtyId,
            @JsonProperty("specialty_name") @Schema(example = "Tai Mũi Họng") String specialtyName) {
    }

    @Schema(description = "Danh mục công khai.")
    public record PublicCatalogResponse(
            List<SpecialtyInfo> specialties,
            List<DoctorInfo> doctors,
            @Schema(example = "2026-11-23T08:00+07:00") String now,
            @JsonProperty("clock_mode") @Schema(allowableValues = { "demo", "real" }) String clockMode) {
    }

    @Schema(description = "Mẫu giờ làm việc tuần của một bác sĩ.")
    public record ScheduleInfo(
            @JsonProperty("doctor_id") long doctorId,
            @Schema(description = "Các ngày làm việc, CSV hoặc danh sách: 0=Thứ Hai ... 5=Thứ Bảy.", example = "0,1,2,3,4") Object weekdays,
            @JsonProperty("start_time") @Schema(example = "08:00") String startTime,
            @JsonProperty("end_time") @Schema(example = "16:00") String endTime) {
    }

    @Schema(description = "Danh mục đầy đủ dành cho người đã đăng nhập, kèm mẫu giờ làm việc.")
    public record CatalogResponse(
            List<SpecialtyInfo> specialties,
            List<DoctorInfo> doctors,
            List<ScheduleInfo> schedules) {
    }

    @Schema(description = "Khung giờ còn trống.")
    public record SlotInfo(
            @Schema(example = "101") long id,
            @JsonProperty("doctor_id") @Schema(description = "Chỉ có ở /api/slots.", example = "2") Long doctorId,
            @JsonProperty("starts_at") @Schema(example = "2026-11-24T08:00+07:00") String startsAt,
            @JsonProperty("ends_at") @Schema(example = "2026-11-24T08:30+07:00") String endsAt) {
    }

    @Schema(description = "Danh sách khung giờ còn trống trong ngày.")
    public record SlotsResponse(List<SlotInfo> slots) {
    }

    // ---------------------------------------------------------------- appointments









    // ---------------------------------------------------------------- leaves






    // ---------------------------------------------------------------- schedules & admin

    @Schema(description = "Mẫu giờ làm việc tuần của bác sĩ.")
    public record SaveScheduleRequest(
            @JsonProperty("doctor_id") @Schema(description = "Id bác sĩ đang hoạt động.", example = "2") long doctorId,
            @Schema(description = "Danh sách ngày làm việc không rỗng, mỗi phần tử 0-5 (0=Thứ Hai ... 5=Thứ Bảy).",
                    example = "[0,1,2,3,4]") List<Integer> weekdays,
            @JsonProperty("start_time") @Schema(description = "HH:mm, từ 07:00.", example = "08:00") String startTime,
            @JsonProperty("end_time") @Schema(description = "HH:mm, sau start_time và không quá 17:00.", example = "16:00") String endTime) {
    }

    @Schema(description = "Kết quả lưu mẫu tuần.")
    public record SaveScheduleResponse(
            @Schema(example = "true") boolean ok,
            @Schema(example = "Đã lưu mẫu tuần. Lịch hẹn đã tồn tại được giữ nguyên.") String message) {
    }





}
