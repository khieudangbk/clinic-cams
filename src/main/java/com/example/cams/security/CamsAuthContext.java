package com.example.cams.security;

import com.example.cams.entity.CamsSession;
import com.example.cams.entity.CamsUser;
import com.example.cams.util.exception.CamsApiException;
import com.example.cams.repository.CamsSessionRepository;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.CamsClock;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Resolves the current CAMS user from the session cookie and enforces CSRF, mirroring app.py's
 * Application.user()/role() and the Handler's cookie/CSRF handling. Deliberately separate from
 * Spring Security's own authentication: the generic auth module's JWT filter chain is untouched.
 */
@Component
public class CamsAuthContext {

    public static final String COOKIE_NAME = "cams_session";
    public static final long SESSION_TTL_SECONDS = 8 * 3600;
    private static final String CSRF_HEADER = "X-CSRF-Token";

    private final CamsSessionRepository sessionRepository;
    private final CamsUserRepository userRepository;
    private final CamsClock clock;
    private final SecureRandom random = new SecureRandom();

    public CamsAuthContext(CamsSessionRepository sessionRepository, CamsUserRepository userRepository,
            CamsClock clock) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /** Looks up the session from the request cookie and returns its active owner, or 401. */
    public Authenticated authenticate(HttpServletRequest request) {
        String token = readCookie(request);
        CamsApiException unauthorized = new CamsApiException(401, "Vui lòng đăng nhập.", "unauthorized");
        if (token.isEmpty()) {
            throw unauthorized;
        }
        CamsSession session = sessionRepository.findByTokenHashAndExpiresAfter(sha256Hex(token), Instant.now())
                .orElseThrow(() -> unauthorized);
        CamsUser user = userRepository.findByIdAndActiveTrue(session.getUserId()).orElseThrow(() -> unauthorized);
        return new Authenticated(user, session);
    }

    /** Non-GET requests must echo the session's CSRF token in the X-CSRF-Token header. */
    public void requireCsrf(HttpServletRequest request, CamsSession session) {
        String provided = request.getHeader(CSRF_HEADER);
        boolean ok = provided != null && constantTimeEquals(provided, session.getCsrf());
        if (!ok) {
            throw new CamsApiException(403, "Phiên thao tác không hợp lệ. Tải lại trang.", "csrf_failed");
        }
    }

    /** Creates a new session row and sets the HttpOnly cookie on the response; returns {user, csrf} data. */
    public String issueSession(HttpServletResponse response, CamsUser user) {
        String rawToken = urlSafeToken(32);
        String csrf = urlSafeToken(24);
        CamsSession session = new CamsSession();
        session.setTokenHash(sha256Hex(rawToken));
        session.setUserId(user.getId());
        session.setCsrf(csrf);
        session.setExpires(Instant.now().plusSeconds(SESSION_TTL_SECONDS));
        sessionRepository.save(session);
        setCookie(response, rawToken, SESSION_TTL_SECONDS);
        return csrf;
    }

    public void clearSession(HttpServletResponse response, CamsSession session) {
        sessionRepository.deleteByTokenHash(session.getTokenHash());
        setCookie(response, "", 0);
    }

    private void setCookie(HttpServletResponse response, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private String urlSafeToken(int bytes) {
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private String sha256Hex(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public record Authenticated(CamsUser user, CamsSession session) {
    }
}
