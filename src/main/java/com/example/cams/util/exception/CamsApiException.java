package com.example.cams.util.exception;

/** Business-rule error, mirrors app.py's APIError: {status, message, code} -> {"error": message, "code": code}. */
public class CamsApiException extends RuntimeException {

    private final int status;
    private final String code;

    public CamsApiException(int status, String message) {
        this(status, message, "validation_error");
    }

    public CamsApiException(int status, String message, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
