package com.example.taskreminder2.notification;

import com.example.taskreminder2.data.local.entity.Task;

/**
 * Penjadwal pengingat deadline (Fitur-07, Personal Mode). Abstraksi ini
 * di-inject ke {@code TaskRepository} agar logika data tetap bisa di-unit-test
 * tanpa AlarmManager / emulator — test cukup memakai {@link #NONE} atau mock.
 *
 * <p>Implementasi produksi: {@link AlarmReminderScheduler}.</p>
 */
public interface ReminderScheduler {

    /**
     * Jadwalkan (atau jadwalkan ulang) pengingat untuk {@code task}. Implementasi
     * yang memutuskan apakah task layak dijadwalkan — mis. hanya yang berdeadline
     * di masa depan dan belum selesai; selain itu pengingatnya dibatalkan.
     */
    void schedule(Task task);

    /** Batalkan pengingat task dengan id tersebut (dipanggil saat hapus/selesai). */
    void cancel(int taskId);

    /** Implementasi no-op untuk unit test / saat penjadwalan tidak relevan. */
    ReminderScheduler NONE = new ReminderScheduler() {
        @Override
        public void schedule(Task task) {
        }

        @Override
        public void cancel(int taskId) {
        }
    };
}
