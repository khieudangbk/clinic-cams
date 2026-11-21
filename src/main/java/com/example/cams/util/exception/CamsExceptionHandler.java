package com.example.cams.util.exception;

import com.example.cams.controller.CamsAdminController;
import com.example.cams.controller.CamsAppointmentController;
import com.example.cams.controller.CamsCatalogController;
import com.example.cams.controller.CamsLeaveController;
import com.example.cams.controller.CamsPublicController;
import com.example.cams.controller.CamsScheduleController;
import com.example.cams.controller.CamsSessionController;
import com.example.cams.dto.cams.CamsApiModels.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Scoped to the CAMS booking controllers only, so these endpoints answer with the same flat
 * {"error": "...", "code": "..."} shape as app.py — never the {success,message,data,...} envelope
 * the unrelated generic auth module's advices (GlobalExceptionHandler, FormatResponse) apply to
 * their own single controller (AuthController).
 */
@RestControllerAdvice(assignableTypes = { CamsPublicController.class, CamsSessionController.class,
        CamsCatalogController.class, CamsAppointmentController.class, CamsLeaveController.class,
        CamsScheduleController.class, CamsAdminController.class })
public class CamsExceptionHandler {

    @ExceptionHandler(CamsApiException.class)
    public ResponseEntity<ErrorResponse> handleApiError(CamsApiException e) {
        return body(e.getStatus(), e.getMessage(), e.getCode());
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, JsonProcessingException.class })
    public ResponseEntity<ErrorResponse> handleMalformed(Exception e) {
        return body(400, "JSON hoặc tham số không hợp lệ.", "bad_request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        e.printStackTrace();
        return body(500, "Lỗi máy chủ.", "internal_error");
    }

    private ResponseEntity<ErrorResponse> body(int status, String message, String code) {
        ErrorResponse payload = new ErrorResponse(message, code);
        return ResponseEntity.status(status).body(payload);
    }
}
