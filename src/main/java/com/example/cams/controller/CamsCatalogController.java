package com.example.cams.controller;

import com.example.cams.config.OpenApiConfig;
import com.example.cams.dto.response.catalog.CatalogResponse;
import com.example.cams.dto.response.slot.SlotInfo;
import com.example.cams.dto.response.slot.SlotsResponse;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.security.CamsAuthContext;
import com.example.cams.security.CamsOriginGuard;
import com.example.cams.service.CamsCatalogService;
import com.example.cams.util.CamsTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
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
    @ApiResponse(responseCode = "200", description = "Danh mục", content = @Content(schema = @Schema(implementation = CatalogResponse.class)))
    @GetMapping("/catalog")
    public CatalogResponse catalog(HttpServletRequest request) {
        originGuard.check(request);
        authContext.authenticate(request);
        return catalogService.fullCatalog();
    }

    @Operation(summary = "Khung giờ còn trống (đã đăng nhập)",
            description = "Như `/api/public/slots` nhưng bao gồm doctor_id. Bác sĩ chỉ được xem lịch của chính mình.")
    @SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
    @ApiResponse(responseCode = "200", description = "Danh sách khung giờ", content = @Content(schema = @Schema(implementation = SlotsResponse.class)))
    @GetMapping("/slots")
    public SlotsResponse slots(HttpServletRequest request, @Parameter(description = "Id bác sĩ", example = "2") @RequestParam("doctor_id") String doctorIdParam,
            @Parameter(description = "Ngày cần xem, định dạng yyyy-MM-dd", example = "2026-11-24") @RequestParam("day") String dayParam) {
        originGuard.check(request);
        CamsUser user = authContext.authenticate(request).user();
        require(Set.of(CamsRole.patient, CamsRole.receptionist, CamsRole.manager, CamsRole.doctor)
                .contains(user.getRole()), "Bạn không có quyền thực hiện thao tác này.", 403, "forbidden");
        long doctorId = integer(doctorIdParam, "Bác sĩ");
        if (user.getRole() == CamsRole.doctor) {
            require(doctorId == user.getId(), "Chỉ được xem lịch của mình.", 403);
        }
        LocalDate day = catalogService.requireDay(dayParam);
        catalogService.generateSlots(doctorId, day);
        List<SlotInfo> slots = catalogService.listSlotsForDay(doctorId, day).stream()
                .filter(slot -> isAvailable(slot.getId()))
                .map(slot -> {
                    return new SlotInfo(
                            slot.getId(),
                            slot.getDoctorId(),
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
}
