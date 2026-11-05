package com.example.cams.security;

import com.example.cams.util.exception.CamsApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.HashSet;

/**
 * Mirrors app.py's per-request Host and Origin checks, called explicitly from every CAMS booking
 * controller method (not registered as a servlet Filter, so it never touches the unrelated
 * generic auth module's own requests/tests). Every mutating /api/* request must come from this
 * same server — this blocks a page on another origin from reaching the CSRF check at all.
 */
@Component
public class CamsOriginGuard {

    public void check(HttpServletRequest request) {
        String host = request.getHeader("Host");
        String hostOnly = host == null ? "" : host.split(":")[0];
        if (!Set.of("localhost", "127.0.0.1").contains(hostOnly)) {
            throw new CamsApiException(403, "Host không được phép.", "forbidden_host");
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            String origin = request.getHeader("Origin");
            int port = request.getServerPort();
            // A HashSet (not Set.of) because these three candidates legitimately collide when
            // Host already is "127.0.0.1:<port>" or "localhost:<port>" — Set.of rejects duplicates.
            Set<String> allowed = new HashSet<>();
            allowed.add("http://" + host);
            allowed.add("http://127.0.0.1:" + port);
            allowed.add("http://localhost:" + port);
            if (origin != null && !allowed.contains(origin)) {
                throw new CamsApiException(403, "Nguồn yêu cầu không hợp lệ.", "forbidden");
            }
        }
    }
}
