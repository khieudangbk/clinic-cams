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

import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.example.cams.entity.enums.CamsRole;

import static com.example.cams.util.CamsValidation.require;

/** Doctor weekly working-hours template, set by a manager. */
@Tag(name = "Schedules", description = "Mẫu giờ làm việc theo tuần của bác sĩ (quản lý).")
@RestController
@RequestMapping("/api/schedules")
public class CamsScheduleController {

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsScheduleService scheduleService;

    public CamsScheduleController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsScheduleService scheduleService) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "Lưu mẫu giờ làm việc tuần",
            description = "Chỉ quản lý. Đặt các ngày và giờ làm việc lặp lại hằng tuần của bác sĩ; áp dụng cho khung giờ sinh về sau, lịch hẹn đã tồn tại được giữ nguyên.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã lưu", content = @Content(schema = @Schema(implementation = CamsApiModels.SaveScheduleResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Mẫu giờ làm việc tuần", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.SaveScheduleRequest.class)))
    @PostMapping
    public Map<String, Object> save(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        require(auth.user().getRole() == CamsRole.manager, "Bạn không có quyền thực hiện thao tác này.", 403,
                "forbidden");
        authContext.requireCsrf(request, auth.session());
        scheduleService.save(body);
        return Map.of("ok", true, "message", "Đã lưu mẫu tuần. Lịch hẹn đã tồn tại được giữ nguyên.");
    }
}
