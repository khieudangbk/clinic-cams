package com.example.cams.util.exception;

import com.example.cams.controller.CamsAdminController;
import com.example.cams.controller.CamsAppointmentController;
import com.example.cams.controller.CamsCatalogController;
import com.example.cams.controller.CamsLeaveController;
import com.example.cams.controller.CamsPublicController;
import com.example.cams.controller.CamsScheduleController;
import com.example.cams.controller.CamsSessionController;
import com.example.cams.dto.response.common.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Returns the CAMS API error payload with error and code fields. */
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
