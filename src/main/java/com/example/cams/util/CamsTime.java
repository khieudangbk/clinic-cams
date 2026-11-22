package com.example.cams.util;

import com.example.cams.util.exception.CamsApiException;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Mirrors app.py's stamp()/parse_datetime() against the Asia/Ho_Chi_Minh (UTC+7) offset. */
public final class CamsTime {

    public static final ZoneOffset OFFSET = ZoneOffset.of("+07:00");
    private static final DateTimeFormatter STAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmxxx");

    private CamsTime() {
    }

    /** ISO-8601, minute precision, always expressed in +07:00 — e.g. "2026-11-23T08:00+07:00". */
    public static String stamp(OffsetDateTime value) {
        return value.withOffsetSameInstant(OFFSET).format(STAMP_FORMAT);
    }

    /** Parses any ISO-8601 datetime that carries an explicit offset; rejects naive (offset-less) values. */
    public static OffsetDateTime parseDatetime(String value) {
        if (value == null) {
            throw new CamsApiException(400, "Thời gian không hợp lệ.");
        }
        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(OFFSET);
        } catch (DateTimeException e) {
            throw new CamsApiException(400, "Thời gian không hợp lệ.");
        }
    }
}
