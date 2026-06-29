package com.example.taskreminder2.notification;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base receiver untuk pekerjaan singkat di background dari {@code onReceive}:
 * {@code goAsync()} menjaga proses tetap hidup sampai pekerjaan selesai, lalu
 * {@code PendingResult.finish()} dipanggil. Dipakai {@link AlarmReceiver} &
 * {@link BootReceiver} (Day 25–26) yang sama-sama perlu akses DB/penjadwalan
 * dari receiver.
 */
public abstract class BackgroundBroadcastReceiver extends BroadcastReceiver {

    /** Pekerjaan latar yang menerima {@link Application} context. */
    protected interface BackgroundWork {
        void run(Application app);
    }

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    /**
     * Jalankan {@code work} di thread background. Aman dipanggil dari
     * {@code onReceive}; proses dijaga hidup hingga {@code work} selesai.
     */
    protected void runAsync(Context context, BackgroundWork work) {
        final PendingResult pending = goAsync();
        final Application app = (Application) context.getApplicationContext();
        IO.execute(() -> {
            try {
                work.run(app);
            } finally {
                pending.finish();
            }
        });
    }
}
