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

}
