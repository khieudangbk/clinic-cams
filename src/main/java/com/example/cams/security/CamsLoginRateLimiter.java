package com.example.cams.security;

import com.example.cams.util.exception.CamsApiException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** Mirrors app.py's in-memory login_failures: 5 failed attempts per email locks that email out for 60s. */
@Component
public class CamsLoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, AtomicReference<long[]>> failures = new ConcurrentHashMap<>();

    /** long[]{count, lastFailureEpochMillis} */
    public void requireNotLocked(String email) {
        long[] state = failures.getOrDefault(email, new AtomicReference<>(new long[] { 0, 0 })).get();
        boolean locked = state[0] >= MAX_ATTEMPTS && System.currentTimeMillis() - state[1] < WINDOW_MILLIS;
        if (locked) {
            throw new CamsApiException(429, "Thử lại sau 60 giây.", "rate_limited");
        }
    }

    public void recordFailure(String email) {
        failures.compute(email, (key, ref) -> {
            long[] previous = ref == null ? new long[] { 0, 0 } : ref.get();
            AtomicReference<long[]> next = new AtomicReference<>(
                    new long[] { previous[0] + 1, System.currentTimeMillis() });
            return next;
        });
    }

    public void clear(String email) {
        failures.remove(email);
    }
}
