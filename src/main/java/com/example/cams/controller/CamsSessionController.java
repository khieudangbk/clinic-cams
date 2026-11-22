package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.response.account.SessionResponse;
import com.example.cams.dto.response.common.OkResponse;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsCatalogService;
import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.cams.service.CamsAdminService.publicUser;

/** The current session: who is logged in, their CSRF token, settings, and logout. */
@Tag(name = "Session", description = "Phiên đăng nhập hiện tại và đăng xuất.")
@RestController
@RequestMapping("/api")
public class CamsSessionController {

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsCatalogService catalogService;
    private final CamsClock clock;

    public CamsSessionController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsCatalogService catalogService, CamsClock clock) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.catalogService = catalogService;
        this.clock = clock;
    }

    @Operation(summary = "Phiên hiện tại",
            description = "Trả về người dùng đang đăng nhập, CSRF token, cấu hình hệ thống và giờ hệ thống.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Thông tin phiên", content = @Content(schema = @Schema(implementation = SessionResponse.class)))
    @GetMapping("/session")
    public SessionResponse session(HttpServletRequest request) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        return new SessionResponse(
                publicUser(auth.user()),
                auth.session().getCsrf(),
                catalogService.settingsMap(),
                CamsTime.stamp(clock.now()));
    }

    @Operation(summary = "Đăng xuất",
            description = "Xóa phiên phía máy chủ và xóa cookie `cams_session`.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã đăng xuất", content = @Content(schema = @Schema(implementation = OkResponse.class)))
    @PostMapping("/logout")
    public OkResponse logout(HttpServletRequest request, HttpServletResponse response) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        authContext.requireCsrf(request, auth.session());
        authContext.clearSession(response, auth.session());
        return new OkResponse(true);
    }
}
