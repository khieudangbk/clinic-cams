package com.example.cams.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Mirrors app.py's Application.now(): a frozen demo clock when CAMS_DEMO_NOW is set
 * (reproducible demos, e.g. "2026-11-23T08:00:00+07:00"), otherwise the real current time.
 */
@Component
public class CamsClock {

    private final OffsetDateTime demoNow;

    public CamsClock(@Value("${cams.demo-now:}") String demoNow) {
        this.demoNow = (demoNow == null || demoNow.isBlank()) ? null : CamsTime.parseDatetime(demoNow);
    }

    public OffsetDateTime now() {
        return demoNow != null ? demoNow : OffsetDateTime.now(CamsTime.OFFSET);
    }

    public boolean isDemo() {
        return demoNow != null;
    }
}
