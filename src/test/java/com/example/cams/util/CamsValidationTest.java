package com.example.cams.util;

import com.example.cams.util.exception.CamsApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamsValidationTest {

    @Test
    void requirePassesWhenConditionHolds() {
        assertDoesNotThrow(() -> CamsValidation.require(true, "x"));
    }

    @Test
    void requireThrowsWithDefaultStatusAndCode() {
        CamsApiException e = assertThrows(CamsApiException.class, () -> CamsValidation.require(false, "bad"));
        assertEquals(400, e.getStatus());
        assertEquals("validation_error", e.getCode());
        assertEquals("bad", e.getMessage());
    }

    @Test
    void requireThrowsWithCustomStatusAndCode() {
        CamsApiException e = assertThrows(CamsApiException.class,
                () -> CamsValidation.require(false, "gone", 409, "conflict"));
        assertEquals(409, e.getStatus());
        assertEquals("conflict", e.getCode());
    }

    @Test
    void textTrimsWhitespace() {
        assertEquals("abc", CamsValidation.text("  abc  ", "Tên"));
    }

    @Test
    void textRejectsNull() {
        assertThrows(CamsApiException.class, () -> CamsValidation.text(null, "Tên"));
    }

    @Test
    void textEnforcesLengthBounds() {
        assertThrows(CamsApiException.class, () -> CamsValidation.text("   ", "Tên"));
        assertThrows(CamsApiException.class, () -> CamsValidation.text("abcdef", "Tên", 5));
        assertThrows(CamsApiException.class, () -> CamsValidation.text("ab", "Tên", 5, 3));
        assertEquals("abcde", CamsValidation.text("abcde", "Tên", 5));
    }

    @Test
    void integerAcceptsLongAndCanonicalString() {
        assertEquals(7L, CamsValidation.requiredInteger(7L, "id"));
        assertEquals(7L, CamsValidation.integer("7", "id"));
        assertEquals(-3L, CamsValidation.integer("-3", "id"));
    }

    @Test
    void integerRejectsNonCanonicalStringsAndNull() {
        assertThrows(CamsApiException.class, () -> CamsValidation.integer("07", "id"));
        assertThrows(CamsApiException.class, () -> CamsValidation.integer("abc", "id"));
        assertThrows(CamsApiException.class, () -> CamsValidation.integer(null, "id"));
    }
}
