package com.noisevisionsoftware.vitema.utils;

import com.google.cloud.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void timestampToLocalDateTime_ShouldConvertTimestampToLocalDateTime() {
        // given
        Date date = Date.from(LocalDateTime.of(2025, 3, 7, 18, 52, 39)
                .atZone(ZoneId.systemDefault()).toInstant());
        Timestamp timestamp = Timestamp.of(date);

        // when
        LocalDateTime result = DateUtils.timestampToLocalDateTime(timestamp);

        // then
        assertEquals(2025, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(7, result.getDayOfMonth());
        assertEquals(18, result.getHour());
        assertEquals(52, result.getMinute());
        assertEquals(39, result.getSecond());
    }

    @Test
    void getWeekNumber_ShouldReturnCorrectWeekNumber() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2025, 3, 7, 18, 52, 39);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp timestamp = Timestamp.of(date);
        int expectedWeekNumber = localDateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        // when
        int result = DateUtils.getWeekNumber(timestamp);

        // then
        assertEquals(expectedWeekNumber, result);
    }

    @Test
    void getWeekNumber_ForFirstWeekOfYear_ShouldReturnOne() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp timestamp = Timestamp.of(date);

        // when
        int result = DateUtils.getWeekNumber(timestamp);

        // then
        assertEquals(1, result);
    }

    @Test
    void getWeekNumber_ForLastWeekOfYear_ShouldReturnCorrectWeek() {
        // given
        LocalDateTime localDateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp timestamp = Timestamp.of(date);
        int expectedWeekNumber = localDateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        // when
        int result = DateUtils.getWeekNumber(timestamp);

        // then
        assertEquals(expectedWeekNumber, result);
    }
}