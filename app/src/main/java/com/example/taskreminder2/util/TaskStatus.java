package com.example.taskreminder2.util;

/**
 * Konstanta status tugas (nilai kanonik yang DISIMPAN di database).
 *
 * <p>Label ramah-pengguna ada di {@code R.array.status_labels} dengan urutan
 * paralel terhadap {@link #VALUES}. Fitur-08 (overdue) membandingkan status
 * dengan {@link #DONE}, jadi nilai kanonik ini harus stabil.</p>
 */
public final class TaskStatus {

    public static final String NOT_STARTED = "BELUM_MULAI";
    public static final String IN_PROGRESS = "SEDANG_DIKERJAKAN";
    public static final String DONE = "SELESAI";

    /** Urutan harus paralel dengan {@code R.array.status_labels}. */
    public static final String[] VALUES = {NOT_STARTED, IN_PROGRESS, DONE};

    public static int indexOf(String value) {
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private TaskStatus() {
    }
}
