package com.example.taskreminder2.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Format tanggal-waktu tunggal untuk seluruh app (sebelumnya diduplikasi di
 * beberapa adapter/Activity). {@link SimpleDateFormat} tidak thread-safe, jadi
 * akses dibungkus {@code synchronized}.
 */
public final class DateTimeFormatter {

    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    public static synchronized String formatDateTime(long epochMillis) {
        return FORMAT.format(new Date(epochMillis));
    }

    private DateTimeFormatter() {
    }
}
