package com.example.taskreminder2.util;

/**
 * Jenis baris pada {@code task_logs} (lihat blueprint Bagian 3.1).
 *
 * <ul>
 *   <li>{@link #ACTIVITY} — dicatat otomatis sistem (Fitur-02).</li>
 *   <li>{@link #NOTE} — ditulis manual oleh user (Fitur-05).</li>
 * </ul>
 */
public final class LogType {

    public static final String ACTIVITY = "ACTIVITY";
    public static final String NOTE = "NOTE";

    private LogType() {
    }
}
