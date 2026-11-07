package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.doc.CamsApiModels;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.cams.entity.CamsUser;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsAppointmentService;
import com.example.cams.util.exception.CamsApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Appointment booking, status transitions, reschedule/cancel and audit trail. */
@Tag(name = "Appointments", description = "Đặt lịch, duyệt, đổi/hủy, check-in, hoàn tất và lịch sử thao tác.")
@RestController
@RequestMapping("/api/appointments")
public class CamsAppointmentController {

    private static final Set<String> ACTIONS = Set.of("approve", "reject", "cancel", "reschedule", "checkin",
            "complete", "noshow");

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsAppointmentService appointmentService;

    public CamsAppointmentController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsAppointmentService appointmentService) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Danh sách lịch hẹn",
            description = "Bệnh nhân thấy lịch của mình, bác sĩ thấy lịch được giao, lễ tân và quản lý thấy tất cả. Sắp xếp theo giờ hẹn.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Danh sách lịch hẹn", content = @Content(schema = @Schema(implementation = CamsApiModels.AppointmentListResponse.class)))
    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        originGuard.check(request);
        CamsUser user = authContext.authenticate(request).user();
        return Map.of("appointments", appointmentService.list(user));
    }

    @Operation(summary = "Đặt lịch khám",
            description = "Bệnh nhân đặt trạng thái `pending` (chờ lễ tân duyệt), kênh `online`. Lễ tân đặt hộ trạng thái `confirmed`, kênh `staff`. Tuân theo giới hạn số lịch chưa khám.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "201", description = "Đã tạo lịch hẹn", content = @Content(schema = @Schema(implementation = CamsApiModels.AppointmentResponse.class)))
    @ApiResponse(responseCode = "409", description = "Khung giờ đã có người đặt hoặc đạt giới hạn lịch chưa khám", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin đặt lịch", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.CreateAppointmentRequest.class)))
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        authContext.requireCsrf(request, auth.session());
        Map<String, Object> appointment = appointmentService.create(auth.user(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("appointment", appointment));
    }

    @Operation(summary = "Lịch sử thao tác của lịch hẹn",
            description = "Danh sách thay đổi trạng thái theo thời gian của một lịch hẹn mà người dùng có quyền xem.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Nhật ký thao tác", content = @Content(schema = @Schema(implementation = CamsApiModels.AuditResponse.class)))
    @ApiResponse(responseCode = "404", description = "Không tìm thấy lịch hẹn", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @GetMapping("/{id}/audit")
    public Map<String, Object> audit(HttpServletRequest request, @Parameter(description = "Id lịch hẹn", example = "7") @PathVariable Long id) {
        originGuard.check(request);
        CamsUser user = authContext.authenticate(request).user();
        List<Map<String, Object>> audit = appointmentService.auditTrail(id, user);
        return Map.of("audit", audit);
    }

    @Operation(summary = "Chuyển trạng thái lịch hẹn",
            description = "Thực hiện một thao tác trên lịch hẹn.\n\n| action | Ai được phép | Điều kiện | Kết quả |\n|---|---|---|---|\n| approve | lễ tân | đang pending | confirmed |\n| reject | lễ tân | đang pending, bắt buộc reason | cancelled |\n| checkin | lễ tân | confirmed, đúng ngày hẹn | arrived |\n| complete | lễ tân | arrived | completed |\n| noshow | lễ tân | confirmed, khung giờ đã kết thúc | noshow |\n| cancel | bệnh nhân, lễ tân | pending/confirmed, trước giờ hẹn tối thiểu cutoff_hours | cancelled |\n| reschedule | bệnh nhân, lễ tân | như cancel, thêm slot_id mới và chưa quá max_reschedules | giữ trạng thái, đổi khung giờ |")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Lịch hẹn sau khi cập nhật", content = @Content(schema = @Schema(implementation = CamsApiModels.AppointmentResponse.class)))
    @ApiResponse(responseCode = "404", description = "action không tồn tại hoặc không tìm thấy lịch hẹn (not_found)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "invalid_transition, cutoff, reschedule_limit, wrong_day, too_early", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lý do và/hoặc khung giờ mới tùy action", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.TransitionRequest.class)))
    @PostMapping("/{id}/{action}")
    public Map<String, Object> transition(HttpServletRequest request, @Parameter(description = "Id lịch hẹn", example = "7") @PathVariable Long id,
            @Parameter(description = "Thao tác", schema = @Schema(allowableValues = { "approve", "reject", "cancel", "reschedule", "checkin", "complete", "noshow" })) @PathVariable String action, @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        if (!ACTIONS.contains(action)) {
            throw new CamsApiException(404, "Không tìm thấy chức năng.", "not_found");
        }
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        authContext.requireCsrf(request, auth.session());
        Map<String, Object> appointment = appointmentService.transition(id, action, auth.user(), body);
        return Map.of("appointment", appointment);
    }
}
