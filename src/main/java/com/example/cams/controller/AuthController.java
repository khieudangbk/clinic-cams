package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.request.auth.LoginRequest;
import com.example.cams.dto.request.auth.RegisterRequest;
import com.example.cams.dto.response.auth.AuthResponse;
import com.example.cams.dto.response.auth.LoginResult;
import com.example.cams.security.JwtProperties;
import com.example.cams.service.AuthService;
import com.example.cams.util.SecurityUtil;
import com.example.cams.util.annotation.ApiMessage;
import com.example.cams.util.exception.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth (JWT)", description = "Module xác thực JWT độc lập. Phản hồi thành công được bọc trong envelope chung {success, message, data}; lỗi bọc theo GlobalExceptionHandler. Refresh token nằm trong cookie HttpOnly `refreshToken`.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @Operation(summary = "Đăng ký (JWT)", description = "Tạo tài khoản mới cho module JWT. Mật khẩu 8-100 ký tự và phải đủ mạnh (@StrongPassword).")
    @ApiResponse(responseCode = "201", description = "Đăng ký thành công, trả về thông tin người dùng")
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    @ApiResponse(responseCode = "409", description = "Email đã tồn tại")
    @PostMapping("/register")
    @ApiMessage("Register successfully")
    public ResponseEntity<AuthResponse.UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse.UserInfo response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Đăng nhập (JWT)", description = "Trả về accessToken trong body và đặt refresh token vào cookie HttpOnly `refreshToken`.")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công")
    @ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu")
    @PostMapping("/login")
    @ApiMessage("Login successfully")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult response = authService.login(request);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(jwtProperties.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response.authResponse());
    }

    @Operation(summary = "Làm mới token", description = "Dùng cookie `refreshToken` để cấp access token mới và xoay vòng refresh token.")
    @ApiResponse(responseCode = "200", description = "Cấp token mới thành công")
    @ApiResponse(responseCode = "401", description = "Thiếu hoặc hết hạn refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Refresh token (cookie HttpOnly)", in = io.swagger.v3.oas.annotations.enums.ParameterIn.COOKIE) @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty())
            throw new UnauthorizedException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");

        LoginResult newTokens = authService.refresh(refreshToken);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newTokens.refreshToken())
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(jwtProperties.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(newTokens.authResponse());
    }

    @Operation(summary = "Đăng xuất (JWT)", description = "Thu hồi refresh token của người dùng hiện tại và xóa cookie.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_JWT))
    @ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    @PostMapping("/logout")
    @ApiMessage("Logout successfully")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        authService.logout(email);
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @Operation(summary = "Hồ sơ người dùng hiện tại", description = "Trả về thông tin tài khoản gắn với access token.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_JWT))
    @ApiResponse(responseCode = "200", description = "Thông tin người dùng")
    @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    @GetMapping("/account")
    @ApiMessage("Get current user profile successfully")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        AuthResponse.UserInfo response = authService.getCurrentUserProfile(email);
        return ResponseEntity.ok(response);
    }
}
