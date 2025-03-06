package com.noisevisionsoftware.nutrilog.utils;

import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtils {

    public static LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static int getWeekNumber(Timestamp timestamp) {
        LocalDateTime date = timestampToLocalDateTime(timestamp);
        return date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }
}
