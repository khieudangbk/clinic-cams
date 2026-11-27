package com.example.cams.security;

import com.example.cams.util.exception.CamsApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamsOriginGuardTest {

    private final CamsOriginGuard guard = new CamsOriginGuard();

    private MockHttpServletRequest request(String method, String host, String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/x");
        request.setServerPort(8080);
        if (host != null) {
            request.addHeader("Host", host);
        }
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        return request;
    }

    @Test
    void allowsLocalhostAndLoopbackHosts() {
        assertDoesNotThrow(() -> guard.check(request("GET", "localhost:8080", null)));
        assertDoesNotThrow(() -> guard.check(request("GET", "127.0.0.1", null)));
    }

    @Test
    void rejectsForeignOrMissingHost() {
        CamsApiException foreign = assertThrows(CamsApiException.class,
                () -> guard.check(request("GET", "evil.example.com", null)));
        assertEquals("forbidden_host", foreign.getCode());
        assertThrows(CamsApiException.class, () -> guard.check(request("GET", null, null)));
    }

    @Test
    void postWithoutOriginIsAllowed() {
        assertDoesNotThrow(() -> guard.check(request("POST", "127.0.0.1:8080", null)));
    }

    @Test
    void postWithSameOriginIsAllowed() {
        assertDoesNotThrow(() -> guard.check(request("POST", "127.0.0.1:8080", "http://127.0.0.1:8080")));
        assertDoesNotThrow(() -> guard.check(request("POST", "localhost:8080", "http://localhost:8080")));
    }

    @Test
    void postWithForeignOriginIsRejected() {
        CamsApiException e = assertThrows(CamsApiException.class,
                () -> guard.check(request("POST", "127.0.0.1:8080", "http://evil.example.com")));
        assertEquals(403, e.getStatus());
        assertEquals("forbidden", e.getCode());
    }

    @Test
    void getIgnoresOrigin() {
        assertDoesNotThrow(() -> guard.check(request("GET", "127.0.0.1:8080", "http://evil.example.com")));
    }
}
