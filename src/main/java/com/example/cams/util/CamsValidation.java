package com.example.cams.util;

import com.example.cams.util.exception.CamsApiException;

/** Mirrors app.py's require()/text()/integer() helpers so every endpoint validates the same way. */
public final class CamsValidation {

    private CamsValidation() {
    }

    public static void require(boolean condition, String message) {
        require(condition, message, 400, "validation_error");
    }

    public static void require(boolean condition, String message, int status) {
        require(condition, message, status, "validation_error");
    }

    public static void require(boolean condition, String message, int status, String code) {
        if (!condition) {
            throw new CamsApiException(status, message, code);
        }
    }

    public static String text(String value, String label) {
        return text(value, label, 200, 1);
    }

    public static String text(String value, String label, int maximum) {
        return text(value, label, maximum, 1);
    }

    public static String text(String value, String label, int maximum, int minimum) {
        require(value != null, label + " không hợp lệ.");
        String trimmed = value.strip();
        require(trimmed.length() >= minimum && trimmed.length() <= maximum,
                label + ": từ " + minimum + " đến " + maximum + " ký tự.");
        return trimmed;
    }

    public static long requiredInteger(Long value, String label) {
        require(value != null, label + " không hợp lệ.");
        return value;
    }

    /** Parses canonical decimal strings from query parameters and booking details. */
    public static long integer(String value, String label) {
        try {
            long parsed = Long.parseLong(value);
            require(String.valueOf(parsed).equals(value), label + " không hợp lệ.");
            return parsed;
        } catch (NumberFormatException e) {
            throw new CamsApiException(400, label + " không hợp lệ.");
        }
    }
}
