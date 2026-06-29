package com.example.taskreminder2.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.TaskStatus;

/**
 * Implementasi {@link ReminderScheduler} berbasis {@link AlarmManager}.
 *
 * <p>Menjadwalkan alarm tepat di {@code deadline} untuk tugas yang masih relevan
 * (berdeadline, di masa depan, belum {@code SELESAI}). Tugas yang tidak memenuhi
 * syarat — termasuk yang baru saja diselesaikan/diubah deadline-nya ke masa lalu
 * — alarmnya dibatalkan, sehingga {@code schedule()} aman dipanggil di tiap
 * insert/update.</p>
 *
 * <p>{@code requestCode} PendingIntent = id task, jadi penjadwalan ulang menimpa
 * alarm lama untuk task yang sama dan {@code cancel()} bisa menemukannya kembali.</p>
 */
public class AlarmReminderScheduler implements ReminderScheduler {

    private final Context appContext;
    private final AlarmManager alarmManager;

    public AlarmReminderScheduler(Context context) {
        this.appContext = context.getApplicationContext();
        this.alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void schedule(Task task) {
        if (task == null || alarmManager == null) {
            return;
        }
        boolean relevan = task.deadline > 0
                && !TaskStatus.DONE.equals(task.status)
                && task.deadline > System.currentTimeMillis();
        if (!relevan) {
            cancel(task.id);
            return;
        }
        PendingIntent pi = buildPendingIntent(task.id, task.title,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (canScheduleExact(appContext)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.deadline, pi);
        } else {
            // Android 12+ tanpa izin exact: fallback inexact agar app tetap berfungsi
            // (pengingat bisa sedikit tertunda). Lihat blueprint Fitur-07.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.deadline, pi);
        }
    }

    @Override
    public void cancel(int taskId) {
        if (alarmManager == null) {
            return;
        }
        PendingIntent pi = buildPendingIntent(taskId, null,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    private PendingIntent buildPendingIntent(int taskId, String title, int flags) {
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(AlarmReceiver.EXTRA_TASK_TITLE, title);
        return PendingIntent.getBroadcast(appContext, taskId, intent, flags);
    }

    /**
     * Apakah app boleh menjadwalkan exact alarm. Selalu true sebelum Android 12
     * (API 31); sejak itu bergantung pada izin {@code SCHEDULE_EXACT_ALARM} yang
     * sejak Android 14 tidak auto-grant.
     */
    public static boolean canScheduleExact(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return am != null && am.canScheduleExactAlarms();
    }
}
