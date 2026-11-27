package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.request.leave.CreateLeaveRequest;
import com.example.cams.dto.response.leave.CreateLeaveResponse;
import com.example.cams.dto.response.leave.LeaveListResponse;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.example.cams.util.CamsValidation.require;

/** Doctor leave registration and the appointments it affects. */
@Tag(name = "Leaves", description = "Đăng ký nghỉ của bác sĩ và các lịch hẹn bị ảnh hưởng.")
@RestController
@RequestMapping("/api/leaves")
public class CamsLeaveController {

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsLeaveService leaveService;

    public CamsLeaveController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsLeaveService leaveService) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.leaveService = leaveService;
    }

    @Operation(summary = "Danh sách lượt nghỉ",
            description = "Bác sĩ thấy lượt nghỉ của mình, quản lý thấy của tất cả. Chỉ doctor và manager được gọi.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Danh sách lượt nghỉ", content = @Content(schema = @Schema(implementation = LeaveListResponse.class)))
    @GetMapping
    public LeaveListResponse list(HttpServletRequest request) {
        originGuard.check(request);
        CamsUser user = authContext.authenticate(request).user();
        requireDoctorOrManager(user);
        return new LeaveListResponse(leaveService.list(user));
    }

    @Operation(summary = "Đăng ký nghỉ",
            description = "Chặn đặt mới trong khoảng nghỉ và trả về các lịch hẹn đã có bị ảnh hưởng để lễ tân liên hệ. Bác sĩ chỉ đăng ký cho mình; quản lý phải chỉ định doctor_id.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "201", description = "Đã đăng ký nghỉ", content = @Content(schema = @Schema(implementation = CreateLeaveResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Khoảng thời gian nghỉ", required = true,
            content = @Content(schema = @Schema(implementation = CreateLeaveRequest.class)))
    @PostMapping
    public ResponseEntity<CreateLeaveResponse> create(HttpServletRequest request,
            @RequestBody CreateLeaveRequest body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireDoctorOrManager(auth.user());
        authContext.requireCsrf(request, auth.session());
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.create(auth.user(), body));
    }

    private void requireDoctorOrManager(CamsUser user) {
        require(Set.of(CamsRole.doctor, CamsRole.manager).contains(user.getRole()),
                "Bạn không có quyền thực hiện thao tác này.", 403, "forbidden");
    }
}
