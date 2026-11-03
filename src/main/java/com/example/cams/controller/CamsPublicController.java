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
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsAccountService;
import com.example.cams.util.CamsClock;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.cams.util.CamsValidation.integer;

/** Unauthenticated endpoints: health, public catalog/slots, registration and login. */
@Tag(name = "Public", description = "Không cần đăng nhập: health, danh mục, khung giờ, đăng ký, đăng nhập.")
@RestController
@RequestMapping("/api")
public class CamsPublicController {

    private final CamsOriginGuard originGuard;
    private final CamsAccountService accountService;
    private final CamsClock clock;

    public CamsPublicController(CamsOriginGuard originGuard, CamsAccountService accountService, CamsClock clock) {
        this.originGuard = originGuard;
        this.accountService = accountService;
        this.clock = clock;
    }

    @Operation(summary = "Kiểm tra tình trạng dịch vụ",
            description = "Trả về trạng thái dịch vụ và chế độ đồng hồ (demo hoặc thật).")
    @ApiResponse(responseCode = "200", description = "Dịch vụ hoạt động", content = @Content(schema = @Schema(implementation = CamsApiModels.HealthResponse.class)))
    @GetMapping("/health")
    public Map<String, Object> health(HttpServletRequest request) {
        originGuard.check(request);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("ok", true);
        json.put("application", "CAMS");
        json.put("clock_mode", clock.isDemo() ? "demo" : "real");
        return json;
    }

    @Operation(summary = "Đăng ký tài khoản bệnh nhân",
            description = "Tạo tài khoản vai trò patient. Email được chuẩn hóa chữ thường và phải duy nhất.")
    @ApiResponse(responseCode = "201", description = "Đăng ký thành công", content = @Content(schema = @Schema(implementation = CamsApiModels.RegisterResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email đã được sử dụng (email_exists)", content = @Content(schema = @Schema(implementation = CamsApiModels.ErrorResponse.class)))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Thông tin đăng ký", required = true,
            content = @Content(schema = @Schema(implementation = CamsApiModels.RegisterRequest.class)))
    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<Map<String, Object>> register(HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        originGuard.check(request);
        Long id = accountService.register(body);
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", id);
        json.put("message", "Đăng ký thành công. Bạn có thể đăng nhập.");
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(json);
    }

}
