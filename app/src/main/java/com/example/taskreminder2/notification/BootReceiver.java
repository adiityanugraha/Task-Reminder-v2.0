package com.example.taskreminder2.notification;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.taskreminder2.data.repository.TaskRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Menjadwalkan ulang seluruh pengingat deadline setelah perangkat reboot
 * (Fitur-07, Day 26). AlarmManager kehilangan semua alarm saat perangkat mati,
 * jadi tanpa ini reminder akan hilang setiap kali HP di-restart.
 *
 * <p>Pekerjaan DB + penjadwalan dijalankan di background ({@code goAsync()}),
 * dan didelegasikan ke {@link TaskRepository#rescheduleAllReminders()} agar
 * logika penjadwalan tetap terpusat di Repository (receiver tidak menyentuh
 * AlarmManager langsung).</p>
 */
public class BootReceiver extends BroadcastReceiver {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        final PendingResult pending = goAsync();
        final Application app = (Application) context.getApplicationContext();
        IO.execute(() -> {
            try {
                new TaskRepository(app).rescheduleAllReminders();
            } finally {
                pending.finish();
            }
        });
    }
}
