package com.example.cams.security;

import com.example.cams.util.exception.CamsApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamsLoginRateLimiterTest {

    private final CamsLoginRateLimiter limiter = new CamsLoginRateLimiter();

    @Test
    void allowsUnknownEmail() {
        assertDoesNotThrow(() -> limiter.requireNotLocked("new@example.com"));
    }

    @Test
    void allowsUpToFourFailures() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("a@example.com");
        }
        assertDoesNotThrow(() -> limiter.requireNotLocked("a@example.com"));
    }

    @Test
    void locksAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("b@example.com");
        }
        CamsApiException e = assertThrows(CamsApiException.class, () -> limiter.requireNotLocked("b@example.com"));
        assertEquals(429, e.getStatus());
        assertEquals("rate_limited", e.getCode());
    }

    @Test
    void lockIsPerEmail() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("c@example.com");
        }
        assertDoesNotThrow(() -> limiter.requireNotLocked("other@example.com"));
    }

    @Test
    void clearResetsLock() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("d@example.com");
        }
        limiter.clear("d@example.com");
        assertDoesNotThrow(() -> limiter.requireNotLocked("d@example.com"));
    }
}
