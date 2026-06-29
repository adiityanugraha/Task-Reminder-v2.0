package com.example.taskreminder2.notification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.taskreminder2.R;
import com.example.taskreminder2.ui.taskdetail.TaskDetailActivity;

/**
 * Membuat NotificationChannel Personal Mode (API 26+) dan menampilkan
 * notifikasi pengingat deadline (Fitur-07). Channel terpisah agar nanti notif
 * Team Mode (Tahap 6 Bagian B) tidak tercampur.
 *
 * <p>Tap notifikasi membuka {@link TaskDetailActivity} untuk task terkait.</p>
 */
public final class NotificationHelper {

    public static final String CHANNEL_REMINDERS = "personal_reminders";
    /** Channel terpisah untuk perubahan dari anggota lain (Team Mode, Day 27). */
    public static final String CHANNEL_TEAM = "team_changes";

    /** Satu notifikasi perubahan team (id tetap → notif terbaru menggantikan
     *  yang lama, tidak menumpuk). Angka besar agar tak bentrok dengan id
     *  notifikasi Personal yang memakai taskId (kecil). */
    private static final int NOTIF_ID_TEAM_CHANGE = 0x7EA3;

    /** Buat channel sekali (idempoten). Aman dipanggil sebelum tiap notifikasi. */
    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_REMINDERS) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.channel_reminders_desc));
        nm.createNotificationChannel(channel);
    }

    /**
     * Tampilkan notifikasi pengingat untuk sebuah task. Jika izin
     * {@code POST_NOTIFICATIONS} (API 33+) belum diberikan, notifikasi diabaikan
     * diam-diam (tidak crash) — izin diminta dari UI saat app dibuka.
     */
    @SuppressLint("MissingPermission") // dijaga manual oleh hasPostPermission()
    public static void showTaskReminder(Context context, int taskId, String title) {
        ensureChannel(context);
        if (!hasPostPermission(context)) {
            return;
        }

        Intent open = new Intent(context, TaskDetailActivity.class);
        open.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, taskId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String safeTitle = (title == null || title.trim().isEmpty())
                ? context.getString(R.string.notif_reminder_fallback_title)
                : title;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle(context.getString(R.string.notif_reminder_title))
                        .setContentText(context.getString(R.string.notif_reminder_text, safeTitle))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat.from(context).notify(taskId, builder.build());
    }

    /** Buat channel Team sekali (idempoten). */
    public static void ensureTeamChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CHANNEL_TEAM) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_TEAM,
                context.getString(R.string.channel_team_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.channel_team_desc));
        nm.createNotificationChannel(channel);
    }

    /**
     * Notifikasi "ada perubahan dari anggota lain" (Fitur-07 Team, app hidup).
     * {@code title} biasanya nama team, {@code text} ringkasan perubahan.
     * Diabaikan diam-diam bila izin POST_NOTIFICATIONS belum diberikan.
     */
    @SuppressLint("MissingPermission") // dijaga manual oleh hasPostPermission()
    public static void showTeamChange(Context context, String title, String text) {
        ensureTeamChannel(context);
        if (!hasPostPermission(context)) {
            return;
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_TEAM)
                        .setSmallIcon(android.R.drawable.stat_notify_sync)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify(NOTIF_ID_TEAM_CHANGE, builder.build());
    }

    private static boolean hasPostPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private NotificationHelper() {
    }
}
