package com.example.taskreminder2.notification;

import android.content.Context;
import android.content.Intent;

import com.example.taskreminder2.data.repository.TaskRepository;

/**
 * Menjadwalkan ulang seluruh pengingat deadline setelah perangkat reboot
 * (Fitur-07, Day 26). AlarmManager kehilangan semua alarm saat perangkat mati,
 * jadi tanpa ini reminder akan hilang setiap kali HP di-restart.
 *
 * <p>Pekerjaan DB + penjadwalan dijalankan di background (lihat
 * {@link BackgroundBroadcastReceiver}) dan didelegasikan ke
 * {@link TaskRepository#rescheduleAllReminders()} agar logika penjadwalan tetap
 * terpusat di Repository (receiver tidak menyentuh AlarmManager langsung).</p>
 */
public class BootReceiver extends BackgroundBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        runAsync(context, app -> new TaskRepository(app).rescheduleAllReminders());
    }
}
