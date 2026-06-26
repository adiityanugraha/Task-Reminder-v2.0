/**
 * Background & notifikasi (Android framework).
 *
 * <p>Berisi {@code AlarmReceiver} & {@code BootReceiver} (reminder deadline
 * lokal via AlarmManager, Personal & Team Mode) dan {@code TeamSyncWorker}
 * (WorkManager: polling perubahan Team saat app tertutup → notifikasi lokal).
 * Tidak ada FCM/FirebaseMessagingService — notifikasi Team client-side,
 * gratis tanpa server (lihat blueprint Fitur-07).</p>
 */
package com.example.taskreminder2.notification;
