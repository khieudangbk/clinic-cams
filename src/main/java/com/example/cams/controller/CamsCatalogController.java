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
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsCatalogService;
import com.example.cams.util.CamsTime;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.cams.util.CamsValidation.integer;
import static com.example.cams.util.CamsValidation.require;

/** Authenticated catalog and private slot listing — mirrors app.py's /api/catalog and /api/slots. */
@Tag(name = "Catalog", description = "Danh mục và khung giờ dành cho người đã đăng nhập.")
@RestController
@RequestMapping("/api")
public class CamsCatalogController {

    private final CamsOriginGuard originGuard;
    private final CamsAuthContext authContext;
    private final CamsCatalogService catalogService;

    public CamsCatalogController(CamsOriginGuard originGuard, CamsAuthContext authContext,
            CamsCatalogService catalogService) {
        this.originGuard = originGuard;
        this.authContext = authContext;
        this.catalogService = catalogService;
    }

    @Operation(summary = "Danh mục đầy đủ",
            description = "Chuyên khoa, bác sĩ đang hoạt động và mẫu giờ làm việc tuần của từng bác sĩ.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Danh mục", content = @Content(schema = @Schema(implementation = CamsApiModels.CatalogResponse.class)))
    @GetMapping("/catalog")
    public Map<String, Object> catalog(HttpServletRequest request) {
        originGuard.check(request);
        authContext.authenticate(request);
        return catalogService.fullCatalog();
    }

}
