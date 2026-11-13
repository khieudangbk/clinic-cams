package com.example.cams.util.exception;

import com.example.cams.controller.CamsCatalogController;
import com.example.cams.controller.CamsLeaveController;
import com.example.cams.controller.CamsPublicController;
import com.example.cams.controller.CamsScheduleController;
import com.example.cams.controller.CamsSessionController;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scoped to the CAMS booking controllers only, so these endpoints answer with the same flat
 * {"error": "...", "code": "..."} shape as app.py — never the {success,message,data,...} envelope
 * the unrelated generic auth module's advices (GlobalExceptionHandler, FormatResponse) apply to
 * their own single controller (AuthController).
 */
@RestControllerAdvice(assignableTypes = { CamsCatalogController.class, CamsLeaveController.class, CamsPublicController.class, CamsScheduleController.class, CamsSessionController.class })
public class CamsExceptionHandler {

    @ExceptionHandler(CamsApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiError(CamsApiException e) {
        return body(e.getStatus(), e.getMessage(), e.getCode());
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, JsonProcessingException.class })
    public ResponseEntity<Map<String, Object>> handleMalformed(Exception e) {
        return body(400, "JSON hoặc tham số không hợp lệ.", "bad_request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        e.printStackTrace();
        return body(500, "Lỗi máy chủ.", "internal_error");
    }

    private ResponseEntity<Map<String, Object>> body(int status, String message, String code) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", message);
        payload.put("code", code);
        return ResponseEntity.status(status).body(payload);
    }
}
