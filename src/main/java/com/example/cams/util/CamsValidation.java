package com.example.cams.util;

import com.example.cams.util.exception.CamsApiException;
import tools.jackson.databind.JsonNode;

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

    public static String text(Object value, String label) {
        return text(value, label, 200, 1);
    }

    public static String text(Object value, String label, int maximum) {
        return text(value, label, maximum, 1);
    }

    public static String text(Object value, String label, int maximum, int minimum) {
        require(value instanceof String, label + " không hợp lệ.");
        String trimmed = ((String) value).strip();
        require(trimmed.length() >= minimum && trimmed.length() <= maximum,
                label + ": từ " + minimum + " đến " + maximum + " ký tự.");
        return trimmed;
    }

    /** Keeps omitted properties distinct from explicit null in defaults and partial updates. */
    public static Object jsonValueOrDefault(JsonNode node, Object defaultValue) {
        if (node == null || node.isMissingNode()) {
            return defaultValue;
        }
        if (node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node;
    }

    /** Accepts a JSON integer, or a numeric string whose canonical form round-trips exactly (rejects "07", 7.0, true). */
    public static long integer(Object value, String label) {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof String s) {
            try {
                long n = Long.parseLong(s);
                require(String.valueOf(n).equals(s), label + " không hợp lệ.");
                return n;
            } catch (NumberFormatException e) {
                throw new CamsApiException(400, label + " không hợp lệ.");
            }
        }
        throw new CamsApiException(400, label + " không hợp lệ.");
    }
}
