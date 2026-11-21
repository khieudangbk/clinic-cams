package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.cams.CamsApiModels.HealthResponse;
import com.example.cams.dto.cams.CamsApiModels.LoginRequest;
import com.example.cams.dto.cams.CamsApiModels.LoginResponse;
import com.example.cams.dto.cams.CamsApiModels.PublicCatalogResponse;
import com.example.cams.dto.cams.CamsApiModels.RegisterRequest;
import com.example.cams.dto.cams.CamsApiModels.RegisterResponse;
import com.example.cams.dto.cams.CamsApiModels.SlotInfo;
import com.example.cams.dto.cams.CamsApiModels.SlotsResponse;
import com.example.cams.dto.cams.CamsApiModels;
import com.example.cams.entity.CamsUser;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsAccountService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.example.cams.service.CamsAdminService.publicUser;
import static com.example.cams.util.CamsValidation.integer;

/** Unauthenticated endpoints: health, public catalog/slots, registration and login. */
@Tag(name = "Public", description = "Không cần đăng nhập: health, danh mục, khung giờ, đăng ký, đăng nhập.")
@RestController
@RequestMapping("/api")
public class CamsPublicController {

    private final CamsOriginGuard originGuard;
    private final CamsCatalogService catalogService;
    private final CamsAccountService accountService;
    private final CamsAuthContext authContext;
    private final CamsClock clock;

    public CamsPublicController(CamsOriginGuard originGuard, CamsCatalogService catalogService,
            CamsAccountService accountService, CamsAuthContext authContext, CamsClock clock) {
        this.originGuard = originGuard;
        this.catalogService = catalogService;
        this.accountService = accountService;
        this.authContext = authContext;
        this.clock = clock;
    }

    @Operation(summary = "Kiểm tra tình trạng dịch vụ",
            description = "Trả về trạng thái dịch vụ và chế độ đồng hồ (demo hoặc thật).")
    @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động", content = @Content(schema = @Schema(implementation = CamsApiModels.HealthResponse.class)))
    @GetMapping("/health")
    public HealthResponse health(HttpServletRequest request) {
        originGuard.check(request);
        return new HealthResponse(true, "CAMS", clock.isDemo() ? "demo" : "real");
    }

    @Operation(summary = "Danh mục công khai",
            description = "Danh sách chuyên khoa và bác sĩ đang hoạt động cùng thời gian hệ thống. Dùng cho trang đặt lịch trước khi đăng nhập.")
    @ApiResponse(responseCode = "200", description = "Danh mục", content = @Content(schema = @Schema(implementation = CamsApiModels.PublicCatalogResponse.class)))
    @GetMapping("/public/catalog")
    public PublicCatalogResponse publicCatalog(HttpServletRequest request) {
        originGuard.check(request);
        return catalogService.publicCatalog();
    }

    @Operation(summary = "Khung giờ còn trống của bác sĩ",
            description = "Sinh (nếu chưa có) và trả về các khung giờ còn trống của một bác sĩ trong một ngày. Khung giờ đã được đặt, đã qua hoặc rơi vào lịch nghỉ bị loại.")
    @ApiResponse(responseCode = "200", description = "Danh sách khung giờ", content = @Content(schema = @Schema(implementation = CamsApiModels.SlotsResponse.class)))
    @ApiResponse(responseCode = "400", description = "doctor_id hoặc day không hợp lệ", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @GetMapping("/public/slots")
    public SlotsResponse publicSlots(HttpServletRequest request,
            @Parameter(description = "Id bác sĩ", example = "2") @RequestParam("doctor_id") String doctorIdParam,
            @Parameter(description = "Ngày cần xem, định dạng yyyy-MM-dd", example = "2026-11-24") @RequestParam("day") String dayParam) {
        originGuard.check(request);
        long doctorId = integer(doctorIdParam, "Bác sĩ");
        LocalDate day = catalogService.requireDay(dayParam);
        catalogService.generateSlots(doctorId, day);
        List<SlotInfo> slots = catalogService.listSlotsForDay(doctorId, day).stream()
                .filter(slot -> isAvailable(slot.getId()))
                .map(slot -> {
                    return new SlotInfo(
                            slot.getId(),
                            null,
                            CamsTime.stamp(slot.getStartsAt()),
                            CamsTime.stamp(slot.getEndsAt()));
                }).toList();
        return new SlotsResponse(slots);
    }

    private boolean isAvailable(Long slotId) {
        try {
            catalogService.validateSlot(slotId, null);
            return true;
        } catch (com.example.cams.util.exception.CamsApiException e) {
            return false;
        }
    }

    @Operation(summary = "Đăng ký tài khoản bệnh nhân",
            description = "Tạo tài khoản vai trò patient. Email được chuẩn hóa chữ thường và phải duy nhất.")
    @ApiResponse(responseCode = "201", description = "Đăng ký thành công", content = @Content(schema = @Schema(implementation = CamsApiModels.RegisterResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email đã được sử dụng (email_exists)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin đăng ký", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.RegisterRequest.class)))
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(HttpServletRequest request,
            @RequestBody RegisterRequest body) {
        originGuard.check(request);
        Long id = accountService.register(body);
        RegisterResponse result = new RegisterResponse(id, "Đăng ký thành công. Bạn có thể đăng nhập.");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @Operation(summary = "Đăng nhập",
            description = "Xác thực email/mật khẩu, đặt cookie HttpOnly `cams_session` (SameSite=Strict, 8 giờ) và trả CSRF token. Sai 5 lần liên tiếp thì khóa email đó 60 giây.")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(schema = @Schema(implementation = CamsApiModels.LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu (invalid_credentials)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Bị khóa tạm thời (rate_limited)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email và mật khẩu", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.LoginRequest.class)))
    @PostMapping("/login")
    public LoginResponse login(HttpServletRequest request, HttpServletResponse response,
            @RequestBody LoginRequest body) {
        originGuard.check(request);
        CamsUser user = accountService.login(body);
        String csrf = authContext.issueSession(response, user);
        return new LoginResponse(publicUser(user), csrf);
    }
}
