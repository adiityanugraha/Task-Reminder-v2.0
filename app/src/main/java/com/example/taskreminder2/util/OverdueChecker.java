package com.example.taskreminder2.util;

/**
 * Utility murni-hitung untuk status terlambat (Fitur-08).
 *
 * <p>TIDAK pernah menulis ke database — status overdue dihitung saat
 * dibutuhkan, tidak disimpan (lihat blueprint Fitur-08). Static & tanpa
 * dependency Android, jadi mudah di-unit-test (Day 12).</p>
 *
 * <p>Dipakai di tiga titik read-only: (1) adapter saat render, (2) query
 * filter {@code TaskDao.filterOverdue} (Day 11), (3) trigger log "Tugas
 * terlambat" dari AlarmReceiver (Day 9).</p>
 */
public final class OverdueChecker {

    /**
     * @return true jika tugas sudah lewat deadline dan belum selesai.
     *         Tugas tanpa deadline ({@code deadline <= 0}) TIDAK pernah
     *         dianggap terlambat.
     */
    public static boolean isOverdue(long deadline, String status) {
        return isOverdue(deadline, status, System.currentTimeMillis());
    }

    /** Varian dengan {@code now} eksplisit — supaya deterministik saat di-test. */
    public static boolean isOverdue(long deadline, String status, long now) {
        return deadline > 0
                && deadline < now
                && !TaskStatus.DONE.equals(status);
    }

    private OverdueChecker() {
    }
}
