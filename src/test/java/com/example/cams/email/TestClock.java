package com.example.cams.email;

import com.example.cams.util.CamsClock;
import com.example.cams.util.CamsTime;

import java.time.OffsetDateTime;

/** A CamsClock the tests can move, so reminder windows are checked against a fixed "now". */
public class TestClock extends CamsClock {

    private volatile OffsetDateTime now;

    public TestClock() {
        super("");
        set("2026-11-23T12:00:00+07:00");
    }

    public void set(String iso) {
        this.now = CamsTime.parseDatetime(iso);
    }

    @Override
    public OffsetDateTime now() {
        return now;
    }

    @Override
    public boolean isDemo() {
        return true;
    }
}
