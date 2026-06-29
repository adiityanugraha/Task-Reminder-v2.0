package com.example.taskreminder2.notification;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.taskreminder2.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Notifikasi Team Mode saat app TERTUTUP (Fitur-07, Day 28) — client-side,
 * tanpa FCM/server (blueprint Bagian 6). Polling periodik (~30 menit; minimum
 * Android = 15 menit) memeriksa task yang {@code updatedAt}-nya lebih baru dari
 * "terakhir dilihat" (disimpan di SharedPreferences) dan diubah anggota lain.
 *
 * <p>Trade-off yang diterima: notif saat app tertutup tertunda hingga interval
 * worker. Saat app hidup, notif real-time ditangani snapshot listener (Day 27).</p>
 */
public class TeamSyncWorker extends Worker {

    private static final String UNIQUE_NAME = "team_sync";
    private static final String PREFS = "team_sync_prefs";
    private static final String KEY_LAST_SEEN = "last_seen_at";

    public TeamSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Jadwalkan polling periodik. Idempoten ({@code KEEP}) — aman dipanggil tiap
     * kali user masuk Team Mode. Hanya berjalan saat ada koneksi (query butuh
     * data segar dari server).
     */
    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                TeamSyncWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    /** Batalkan polling (dipanggil saat logout). */
    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return Result.success(); // tidak login → tidak ada yang dipantau
        }
        String uid = auth.getCurrentUser().getUid();

        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        // Run pertama: simpan baseline saja, jangan notifikasi seluruh riwayat.
        if (!prefs.contains(KEY_LAST_SEEN)) {
            prefs.edit().putLong(KEY_LAST_SEEN, now).apply();
            return Result.success();
        }
        long lastSeen = prefs.getLong(KEY_LAST_SEEN, now);

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentSnapshot userDoc = Tasks.await(db.collection("users").document(uid).get());
            @SuppressWarnings("unchecked")
            List<String> teamIds = (List<String>) userDoc.get("teams");
            if (teamIds == null || teamIds.isEmpty()) {
                prefs.edit().putLong(KEY_LAST_SEEN, now).apply();
                return Result.success();
            }

            int changedByOthers = 0;
            for (String teamId : teamIds) {
                QuerySnapshot snap = Tasks.await(db.collection("teams").document(teamId)
                        .collection("tasks")
                        .whereGreaterThan("updatedAt", lastSeen)
                        .get());
                for (QueryDocumentSnapshot d : snap) {
                    String updatedBy = d.getString("updatedBy");
                    if (updatedBy == null || !updatedBy.equals(uid)) {
                        changedByOthers++;
                    }
                }
            }

            if (changedByOthers > 0) {
                NotificationHelper.showTeamChange(
                        getApplicationContext(),
                        getApplicationContext().getString(R.string.channel_team_name),
                        getApplicationContext().getString(R.string.notif_team_multi, changedByOthers));
            }
            prefs.edit().putLong(KEY_LAST_SEEN, now).apply();
            return Result.success();
        } catch (Exception e) {
            // Jaringan/await gagal: jangan majukan lastSeen, coba lagi nanti.
            return Result.retry();
        }
    }
}
