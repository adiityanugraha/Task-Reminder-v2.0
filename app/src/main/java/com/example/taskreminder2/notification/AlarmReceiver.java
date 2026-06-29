package com.example.taskreminder2.notification;

import android.content.Context;
import android.content.Intent;

import com.example.taskreminder2.data.repository.TaskLogRepository;

/**
 * Menerima alarm deadline (dijadwalkan {@link AlarmReminderScheduler}) lalu
 * menampilkan notifikasi pengingat (Fitur-07).
 *
 * <p>Alarm hanya berbunyi untuk task yang masih relevan — saat task
 * diselesaikan/dihapus/diubah deadline-nya, alarm sudah dibatalkan/dijadwalkan
 * ulang oleh Repository — jadi berbunyinya alarm berarti task itu memang
 * mencapai deadline. Karena itu sekaligus mencatat log "Tugas terlambat" sekali
 * (Fitur-08 → Fitur-02), dipicu dari sini, bukan dari path baca.</p>
 */
public class AlarmReceiver extends BackgroundBroadcastReceiver {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);
        if (taskId <= 0) {
            return;
        }
        String title = intent.getStringExtra(EXTRA_TASK_TITLE);
        NotificationHelper.showTaskReminder(context, taskId, title);

        // Tulis log "Tugas terlambat" di background sampai selesai.
        runAsync(context, app ->
                new TaskLogRepository(app).logActivitySync(taskId, "Tugas terlambat"));
    }
}
