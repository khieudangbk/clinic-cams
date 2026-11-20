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

    @Schema(description = "Kết quả tạo mới, trả về id.")
    public record CreatedIdResponse(@Schema(description = "Id bản ghi vừa tạo.", example = "12") long id) {
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

    @Schema(description = "Thông tin người khám (tùy chọn). Chỉ chấp nhận đúng các trường dưới đây; mỗi trường là chuỗi "
            + "tối đa 100 ký tự (address và extra_note tối đa 500).")
    public record BookingDetails(
            @JsonProperty("booking_for") @Schema(description = "Đặt cho ai. Nếu có, bắt buộc kèm name và phone.",
                    allowableValues = { "self", "relative" }) String bookingFor,
            @Schema(description = "Họ tên người khám.", example = "Nguyễn Thị C") String name,
            @Schema(description = "Điện thoại người khám, 8-20 ký tự.", example = "0988777666") String phone,
            @JsonProperty("birth_year") @Schema(description = "Năm sinh từ 1900 đến năm hiện tại.", example = "1990") String birthYear,
            @Schema(allowableValues = { "", "male", "female", "other" }) String gender,
            @Schema(example = "Hà Nội") String province,
            @Schema(example = "Phường Cầu Giấy") String ward,
            @Schema(example = "12 Xuân Thủy") String address,
            @JsonProperty("extra_note") @Schema(example = "Ho kéo dài 3 ngày") String extraNote,
            @Schema(description = "Hiện chỉ hỗ trợ thanh toán tại phòng khám.", allowableValues = { "at_clinic" }) String payment) {
    }

    @Schema(description = "Yêu cầu đặt lịch. Bệnh nhân đặt cho chính mình; lễ tân phải cung cấp patient_name và "
            + "patient_phone (khách vãng lai) hoặc patient_email (bệnh nhân đã có tài khoản).")
    public record CreateAppointmentRequest(
            @JsonProperty("slot_id") @Schema(description = "Id khung giờ còn trống (lấy từ /slots).", example = "101") long slotId,
            @Schema(description = "Lý do khám, 1-500 ký tự.", example = "Khám tổng quát") String reason,
            @JsonProperty("patient_name") @Schema(description = "Chỉ lễ tân: họ tên khách, 1-100 ký tự.", example = "Khách Vãng Lai") String patientName,
            @JsonProperty("patient_phone") @Schema(description = "Chỉ lễ tân: điện thoại khách, 8-20 ký tự.", example = "0911222333") String patientPhone,
            @JsonProperty("patient_email") @Schema(description = "Chỉ lễ tân: email của bệnh nhân đã có tài khoản.", example = "a@example.com") String patientEmail,
            @JsonProperty("booking_details") BookingDetails bookingDetails) {
    }

    @Schema(description = "Dữ liệu cho thao tác chuyển trạng thái; trường cần dùng tùy theo action.")
    public record TransitionRequest(
            @Schema(description = "cancel/reschedule: bắt buộc với bệnh nhân (tối đa 300 ký tự). reject: bắt buộc. "
                    + "Các action khác: bỏ qua.", example = "Khách đổi ý") String reason,
            @JsonProperty("slot_id") @Schema(description = "Chỉ reschedule: khung giờ mới, phải khác khung giờ hiện tại.", example = "205") Long slotId) {
    }

    @Schema(description = "Một lịch hẹn.")
    public record AppointmentInfo(
            @Schema(example = "7") long id,
            @JsonProperty("patient_id") @Schema(nullable = true) Long patientId,
            @JsonProperty("slot_id") long slotId,
            @JsonProperty("doctor_id") Long doctorId,
            @JsonProperty("starts_at") @Schema(example = "2026-11-24T08:00+07:00") String startsAt,
            @JsonProperty("ends_at") @Schema(example = "2026-11-24T08:30+07:00") String endsAt,
            @JsonProperty("doctor_name") String doctorName,
            @JsonProperty("patient_name") String patientName,
            @JsonProperty("patient_phone") String patientPhone,
            @Schema(allowableValues = { "pending", "confirmed", "arrived", "completed", "cancelled", "noshow" }) String status,
            @Schema(description = "Kênh đặt: online (bệnh nhân) hoặc staff (lễ tân).", allowableValues = { "online", "staff" }) String channel,
            String reason,
            @JsonProperty("reschedule_count") @Schema(description = "Số lần đã đổi lịch.") int rescheduleCount,
            @JsonProperty("created_by") Long createdBy,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("doctor_on_leave") @Schema(description = "1 nếu bác sĩ đang nghỉ trong khung giờ này.", allowableValues = { "0", "1" }) int doctorOnLeave,
            @JsonProperty("booking_details") @Schema(description = "JSON thông tin người khám đã lưu.") String bookingDetails) {
    }

    @Schema(description = "Một lịch hẹn.")
    public record AppointmentResponse(AppointmentInfo appointment) {
    }

    @Schema(description = "Danh sách lịch hẹn theo vai trò: bệnh nhân thấy của mình, bác sĩ thấy của mình, lễ tân/quản lý thấy tất cả.")
    public record AppointmentListResponse(List<AppointmentInfo> appointments) {
    }

    @Schema(description = "Một dòng nhật ký thao tác trên lịch hẹn.")
    public record AuditEntry(
            long id,
            @JsonProperty("appointment_id") long appointmentId,
            @JsonProperty("actor_id") Long actorId,
            @JsonProperty("actor_name") String actorName,
            @Schema(example = "Chuyển trạng thái confirmed") String action,
            @JsonProperty("before_json") @Schema(description = "Trạng thái trước (JSON dạng chuỗi).") String beforeJson,
            @JsonProperty("after_json") @Schema(description = "Trạng thái sau (JSON dạng chuỗi).") String afterJson,
            String reason,
            @JsonProperty("created_at") String createdAt) {
    }

    @Schema(description = "Lịch sử thao tác của một lịch hẹn.")
    public record AuditResponse(List<AuditEntry> audit) {
    }

    // ---------------------------------------------------------------- leaves

    @Schema(description = "Đăng ký nghỉ của bác sĩ.")
    public record CreateLeaveRequest(
            @JsonProperty("doctor_id") @Schema(description = "Quản lý bắt buộc chỉ định; bác sĩ tự đăng ký thì bị bỏ qua.", example = "2") Long doctorId,
            @JsonProperty("starts_at") @Schema(description = "ISO-8601 có offset.", example = "2026-11-25T08:00:00+07:00") String startsAt,
            @JsonProperty("ends_at") @Schema(description = "ISO-8601 có offset, sau starts_at.", example = "2026-11-25T12:00:00+07:00") String endsAt,
            @Schema(description = "Lý do nghỉ, 1-300 ký tự.", example = "Nghỉ ốm") String reason) {
    }

    @Schema(description = "Một lượt nghỉ của bác sĩ.")
    public record LeaveInfo(
            long id,
            @JsonProperty("doctor_id") long doctorId,
            @JsonProperty("doctor_name") String doctorName,
            @JsonProperty("starts_at") String startsAt,
            @JsonProperty("ends_at") String endsAt,
            String reason,
            @JsonProperty("created_by") Long createdBy) {
    }

    @Schema(description = "Danh sách lượt nghỉ: bác sĩ thấy của mình, quản lý thấy tất cả.")
    public record LeaveListResponse(List<LeaveInfo> leaves) {
    }

    @Schema(description = "Lịch hẹn bị ảnh hưởng bởi lượt nghỉ.")
    public record AffectedAppointment(
            long id,
            String status,
            @JsonProperty("starts_at") String startsAt,
            @JsonProperty("patient_name") String patientName,
            @JsonProperty("patient_phone") String patientPhone) {
    }

    @Schema(description = "Kết quả đăng ký nghỉ.")
    public record CreateLeaveResponse(
            long id,
            List<AffectedAppointment> affected,
            @Schema(example = "Đã chặn đặt mới. Lễ tân cần liên hệ các lịch bị ảnh hưởng.") String message) {
    }

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

    @Schema(description = "Danh sách nhân sự (bác sĩ, lễ tân, quản lý).")
    public record UserListResponse(List<UserInfo> users) {
    }

    @Schema(description = "Tạo tài khoản nhân sự.")
    public record CreateStaffUserRequest(
            @Schema(allowableValues = { "doctor", "receptionist" }) String role,
            @Schema(description = "Họ tên, 1-100 ký tự.", example = "BS. Lê D") String name,
            @Schema(description = "Email hợp lệ, không trùng.", example = "d@cams.local") String email,
            @Schema(description = "Mật khẩu 10-128 ký tự.", example = "Demo@12345") String password,
            @JsonProperty("specialty_id") @Schema(description = "Bắt buộc khi role = doctor.", example = "1") Long specialtyId) {
    }

    @Schema(description = "Cập nhật tài khoản nhân sự; chỉ gửi các trường cần đổi.")
    public record UpdateStaffUserRequest(
            @Schema(description = "Họ tên mới, 1-100 ký tự.") String name,
            @Schema(description = "1 = hoạt động, 0 = khóa.", allowableValues = { "0", "1" }) Integer active,
            @JsonProperty("specialty_id") @Schema(description = "Chuyên khoa mới; null để bỏ chuyên khoa.", nullable = true) Long specialtyId) {
    }

    @Schema(description = "Tạo mới hoặc đổi tên chuyên khoa.")
    public record UpsertSpecialtyRequest(
            @Schema(description = "Bỏ trống để tạo mới; có giá trị để đổi tên chuyên khoa đó.", nullable = true) Long id,
            @Schema(description = "Tên chuyên khoa, 1-100 ký tự.", example = "Nhi khoa") String name) {
    }

}
