package com.example.cams.security;

import com.example.cams.entity.CamsSession;
import com.example.cams.repository.CamsSessionRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.CamsClock;
import com.example.cams.util.exception.CamsApiException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CamsAuthContextTest {

    private final CamsAuthContext context = new CamsAuthContext(mock(CamsSessionRepository.class),
            mock(CamsUserRepository.class), new CamsClock(""));

    private CamsSession sessionWithCsrf(String csrf) {
        CamsSession session = new CamsSession();
        session.setCsrf(csrf);
        return session;
    }

    @Test
    void authenticateWithoutCookieIsUnauthorized() {
        CamsApiException e = assertThrows(CamsApiException.class,
                () -> context.authenticate(new MockHttpServletRequest()));
        assertEquals(401, e.getStatus());
        assertEquals("unauthorized", e.getCode());
    }

    @Test
    void authenticateWithUnknownTokenIsUnauthorized() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CamsAuthContext.COOKIE_NAME, "no-such-token"));
        CamsApiException e = assertThrows(CamsApiException.class, () -> context.authenticate(request));
        assertEquals(401, e.getStatus());
    }

    @Test
    void requireCsrfAcceptsMatchingToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-CSRF-Token", "token-1");
        assertDoesNotThrow(() -> context.requireCsrf(request, sessionWithCsrf("token-1")));
    }

    @Test
    void requireCsrfRejectsMissingOrWrongToken() {
        CamsSession session = sessionWithCsrf("token-1");
        CamsApiException missing = assertThrows(CamsApiException.class,
                () -> context.requireCsrf(new MockHttpServletRequest(), session));
        assertEquals(403, missing.getStatus());
        assertEquals("csrf_failed", missing.getCode());

        MockHttpServletRequest wrong = new MockHttpServletRequest();
        wrong.addHeader("X-CSRF-Token", "token-2");
        assertThrows(CamsApiException.class, () -> context.requireCsrf(wrong, session));
    }

    @Test
    void issueSessionSetsHttpOnlyStrictCookieAndReturnsCsrf() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        com.example.cams.entity.CamsUser user = new com.example.cams.entity.CamsUser();
        user.setId(1L);
        String csrf = context.issueSession(response, user);
        assertNotNull(csrf);
        assertTrue(csrf.length() > 10);
        String header = response.getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.startsWith(CamsAuthContext.COOKIE_NAME + "="));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=Strict"));
    }
}
