package com.example.cams.util;

import com.example.cams.util.exception.CamsApiException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamsTimeTest {

    @Test
    void stampConvertsToPlus7AtMinutePrecision() {
        OffsetDateTime utc = OffsetDateTime.of(2026, 11, 23, 1, 30, 45, 0, ZoneOffset.UTC);
        assertEquals("2026-11-23T08:30+07:00", CamsTime.stamp(utc));
    }

    @Test
    void parseDatetimeNormalisesToPlus7() {
        OffsetDateTime parsed = CamsTime.parseDatetime("2026-11-23T01:00:00Z");
        assertEquals(CamsTime.OFFSET, parsed.getOffset());
        assertEquals(8, parsed.getHour());
    }

    @Test
    void parseDatetimeRejectsNaiveAndGarbageAndNull() {
        assertThrows(CamsApiException.class, () -> CamsTime.parseDatetime("2026-11-23T08:00:00"));
        assertThrows(CamsApiException.class, () -> CamsTime.parseDatetime("not a date"));
        assertThrows(CamsApiException.class, () -> CamsTime.parseDatetime(null));
    }

    @Test
    void clockUsesDemoTimeWhenConfigured() {
        CamsClock demo = new CamsClock("2026-11-23T08:00:00+07:00");
        assertEquals(true, demo.isDemo());
        assertEquals("2026-11-23T08:00+07:00", CamsTime.stamp(demo.now()));
    }

    @Test
    void clockUsesRealTimeWhenBlank() {
        CamsClock real = new CamsClock("");
        assertEquals(false, real.isDemo());
        assertEquals(CamsTime.OFFSET, real.now().getOffset());
    }
}
