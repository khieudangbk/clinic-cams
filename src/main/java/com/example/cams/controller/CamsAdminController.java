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

import com.example.cams.entity.enums.CamsRole;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.example.cams.util.CamsValidation.require;

/** Manager-only administration: staff accounts, specialties and global settings. */
@Tag(name = "Admin", description = "Quản lý tài khoản nhân sự, chuyên khoa và cấu hình (chỉ quản lý).")
@RestController
@RequestMapping("/api")
public class CamsAdminController {

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsAdminService adminService;

    public CamsAdminController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsAdminService adminService) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.adminService = adminService;
    }

    private void requireManager(HttpServletRequest request, CamsAuthContext.Authenticated auth, boolean mutating) {
        originGuard.check(request);
        require(auth.user().getRole() == CamsRole.manager, "Bạn không có quyền thực hiện thao tác này.", 403,
                "forbidden");
        if (mutating) {
            authContext.requireCsrf(request, auth.session());
        }
    }

    @Operation(summary = "Danh sách nhân sự",
            description = "Bác sĩ, lễ tân và quản lý, sắp theo id. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Danh sách nhân sự", content = @Content(schema = @Schema(implementation = CamsApiModels.UserListResponse.class)))
    @GetMapping("/users")
    public Map<String, Object> listUsers(HttpServletRequest request) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, false);
        return Map.of("users", adminService.listStaffUsers());
    }

    @Operation(summary = "Tạo tài khoản nhân sự",
            description = "Tạo bác sĩ hoặc lễ tân. Bác sĩ mới được tạo kèm mẫu giờ làm việc mặc định. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "201", description = "Đã tạo", content = @Content(schema = @Schema(implementation = CamsApiModels.CreatedIdResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email đã tồn tại (email_exists)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin nhân sự", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.CreateStaffUserRequest.class)))
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        Long id = adminService.createStaffUser(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "Cập nhật tài khoản nhân sự",
            description = "Đổi tên, khóa/mở khóa hoặc đổi chuyên khoa; chỉ các trường được gửi mới thay đổi. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã cập nhật", content = @Content(schema = @Schema(implementation = CamsApiModels.OkResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tài khoản không thuộc phạm vi quản trị", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Các trường cần đổi", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.UpdateStaffUserRequest.class)))
    @PostMapping("/users/{id}")
    public Map<String, Object> updateUser(HttpServletRequest request, @Parameter(description = "Id tài khoản", example = "4") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        adminService.updateStaffUser(id, body);
        return Map.of("ok", true);
    }

    @Operation(summary = "Tạo hoặc đổi tên chuyên khoa",
            description = "Không gửi id thì tạo mới, gửi id thì đổi tên chuyên khoa đó. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã lưu", content = @Content(schema = @Schema(implementation = CamsApiModels.OkResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Chuyên khoa", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.UpsertSpecialtyRequest.class)))
    @PostMapping("/specialties")
    public Map<String, Object> upsertSpecialty(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        adminService.upsertSpecialty(body);
        return Map.of("ok", true);
    }

}
