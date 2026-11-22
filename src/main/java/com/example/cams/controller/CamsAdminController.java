package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.request.admin.CreateStaffUserRequest;
import com.example.cams.dto.request.admin.UpdateSettingsRequest;
import com.example.cams.dto.request.admin.UpdateStaffUserRequest;
import com.example.cams.dto.request.admin.UpsertSpecialtyRequest;
import com.example.cams.dto.response.admin.UserListResponse;
import com.example.cams.dto.response.common.CreatedIdResponse;
import com.example.cams.dto.response.common.ErrorResponse;
import com.example.cams.dto.response.common.OkResponse;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ApiResponse(responseCode = "200", description = "Danh sách nhân sự", content = @Content(schema = @Schema(implementation = UserListResponse.class)))
    @GetMapping("/users")
    public UserListResponse listUsers(HttpServletRequest request) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, false);
        return new UserListResponse(adminService.listStaffUsers());
    }

    @Operation(summary = "Tạo tài khoản nhân sự",
            description = "Tạo bác sĩ hoặc lễ tân. Bác sĩ mới được tạo kèm mẫu giờ làm việc mặc định. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "201", description = "Đã tạo", content = @Content(schema = @Schema(implementation = CreatedIdResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email đã tồn tại (email_exists)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin nhân sự", required = true,
            content = @Content(schema = @Schema(implementation = CreateStaffUserRequest.class)))
    @PostMapping("/users")
    public ResponseEntity<CreatedIdResponse> createUser(HttpServletRequest request,
            @RequestBody CreateStaffUserRequest body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        Long id = adminService.createStaffUser(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatedIdResponse(id));
    }

    @Operation(summary = "Cập nhật tài khoản nhân sự",
            description = "Đổi tên, khóa/mở khóa hoặc đổi chuyên khoa; chỉ các trường được gửi mới thay đổi. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã cập nhật", content = @Content(schema = @Schema(implementation = OkResponse.class)))
    @ApiResponse(responseCode = "404", description = "Tài khoản không thuộc phạm vi quản trị", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Các trường cần đổi", required = true,
            content = @Content(schema = @Schema(implementation = UpdateStaffUserRequest.class)))
    @PostMapping("/users/{id}")
    public OkResponse updateUser(HttpServletRequest request, @Parameter(description = "Id tài khoản", example = "4") @PathVariable Long id,
            @RequestBody UpdateStaffUserRequest body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        adminService.updateStaffUser(id, body);
        return new OkResponse(true);
    }

    @Operation(summary = "Tạo hoặc đổi tên chuyên khoa",
            description = "Không gửi id thì tạo mới, gửi id thì đổi tên chuyên khoa đó. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã lưu", content = @Content(schema = @Schema(implementation = OkResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Chuyên khoa", required = true,
            content = @Content(schema = @Schema(implementation = UpsertSpecialtyRequest.class)))
    @PostMapping("/specialties")
    public OkResponse upsertSpecialty(HttpServletRequest request, @RequestBody UpsertSpecialtyRequest body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        adminService.upsertSpecialty(body);
        return new OkResponse(true);
    }

    @Operation(summary = "Cập nhật cấu hình",
            description = "Chỉ chấp nhận slot_minutes, cutoff_hours, max_reschedules, mỗi giá trị trong khoảng quy định. Chỉ quản lý.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @SecurityRequirement(name = OpenApiConfig.CSRF_HEADER)
    @ApiResponse(responseCode = "200", description = "Đã lưu", content = @Content(schema = @Schema(implementation = OkResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cấu hình cần đổi", required = true,
            content = @Content(schema = @Schema(implementation = UpdateSettingsRequest.class)))
    @PostMapping("/settings")
    public OkResponse updateSettings(HttpServletRequest request, @RequestBody UpdateSettingsRequest body) {
        originGuard.check(request);
        CamsAuthContext.Authenticated auth = authContext.authenticate(request);
        requireManager(request, auth, true);
        adminService.updateSettings(body);
        return new OkResponse(true);
    }
}
