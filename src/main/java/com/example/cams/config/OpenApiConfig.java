package com.example.cams.config;

import com.example.cams.controller.AuthController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Swagger/OpenAPI metadata and the three authentication schemes the API uses. */
@Configuration
public class OpenApiConfig {

    public static final String SESSION_COOKIE = "camsSession";
    public static final String CSRF_HEADER = "csrfToken";
    public static final String BEARER_JWT = "bearerJwt";

    @Bean
    public OpenAPI camsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAMS - Clinic Appointment Management System API")
                        .version("0.0.1")
                        .description("""
                                REST API đặt lịch khám của phòng khám.

                                **Hai nhóm API:**
                                - `/api/**` (trừ `/api/auth/**`): API nghiệp vụ CAMS. Đăng nhập bằng `POST /api/login` để \
                                nhận cookie HttpOnly `cams_session` và `csrf`. Mọi request ghi (POST) phải gửi header \
                                `X-CSRF-Token` bằng giá trị `csrf` đó.
                                - `/api/auth/**`: module xác thực JWT độc lập (bearer token + cookie `refreshToken`).

                                **Quy tắc chung:** Host chỉ được là `localhost` hoặc `127.0.0.1`; request POST có header \
                                `Origin` phải cùng nguồn. Lỗi nghiệp vụ trả về dạng `{"error": "...", "code": "..."}`.

                                **Vai trò:** `patient` (bệnh nhân), `receptionist` (lễ tân), `doctor` (bác sĩ), \
                                `manager` (quản lý). Thời gian luôn ở múi giờ +07:00.
                                """))
                .tags(List.of(
                        new Tag().name("Public").description("Không cần đăng nhập: health, danh mục, khung giờ, đăng ký, đăng nhập."),
                        new Tag().name("Session").description("Phiên đăng nhập hiện tại và đăng xuất."),
                        new Tag().name("Catalog").description("Danh mục và khung giờ dành cho người đã đăng nhập."),
                        new Tag().name("Appointments").description("Đặt lịch, duyệt, đổi/hủy, check-in, hoàn tất và lịch sử thao tác."),
                        new Tag().name("Leaves").description("Đăng ký nghỉ của bác sĩ và các lịch hẹn bị ảnh hưởng."),
                        new Tag().name("Schedules").description("Mẫu giờ làm việc theo tuần của bác sĩ (quản lý)."),
                        new Tag().name("Admin").description("Quản lý tài khoản nhân sự, chuyên khoa và cấu hình (quản lý)."),
                        new Tag().name("Auth (JWT)").description("Module xác thực JWT độc lập, bọc kết quả trong envelope chung.")))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("cams_session")
                                .description("Cookie HttpOnly do `POST /api/login` đặt; hiệu lực 8 giờ."))
                        .addSecuritySchemes(CSRF_HEADER, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-CSRF-Token")
                                .description("Giá trị `csrf` trả về khi đăng nhập hoặc từ `GET /api/session`; bắt buộc cho request ghi."))
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("Access token từ `POST /api/auth/login`.")));
    }

    /**
     * Adds the shared error responses to every CAMS booking operation so each endpoint does not have to
     * repeat them: 400 (validation), 403 (forbidden/CSRF/origin) and 500, plus 401 on authenticated ones.
     */
    @Bean
    public OperationCustomizer commonErrorResponses() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
            if (handlerMethod.getBeanType() == AuthController.class) {
                return operation;
            }
            boolean authenticated = operation.getSecurity() != null && !operation.getSecurity().isEmpty();
            addIfAbsent(operation, "400", "Dữ liệu không hợp lệ (validation_error, bad_request)");
            if (authenticated) {
                addIfAbsent(operation, "401", "Chưa đăng nhập hoặc phiên hết hạn (unauthorized)");
            }
            addIfAbsent(operation, "403", "Không đủ quyền, thiếu/sai CSRF token, Host hoặc Origin không hợp lệ "
                    + "(forbidden, csrf_failed, forbidden_host)");
            addIfAbsent(operation, "500", "Lỗi máy chủ (internal_error)");
            return operation;
        };
    }

    private static void addIfAbsent(Operation operation, String code, String description) {
        if (operation.getResponses().containsKey(code)) {
            return;
        }
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        operation.getResponses().addApiResponse(code, new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json", new MediaType().schema(ref))));
    }
}
