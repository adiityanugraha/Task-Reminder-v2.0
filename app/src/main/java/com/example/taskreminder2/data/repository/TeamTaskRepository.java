package com.example.taskreminder2.data.repository;

import androidx.annotation.Nullable;

import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.model.TeamTaskChange;
import com.example.taskreminder2.data.model.TeamTaskLog;
import com.example.taskreminder2.util.LogType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Akses tugas Team Mode — menulis LANGSUNG ke Firestore
 * ({@code teams/{teamId}/tasks}). Tidak ada cache Room / SyncEngine: saat
 * offline, Firestore mengantre tulisan dan mengirimnya otomatis ketika online
 * (offline persistence default aktif di Android). Read realtime via snapshot
 * listener ditambahkan Day 19; di sini disediakan {@link #loadTasksOnce} untuk
 * verifikasi awal.
 */
public class TeamTaskRepository {

    /** Hasil operasi tulis. {@code taskId} = id dokumen terkait. */
    public interface TaskCallback {
        void onResult(boolean success, @Nullable String taskId, @Nullable String errorMessage);
    }

    public interface TasksCallback {
        void onResult(List<TeamTask> tasks);
    }

    public interface TaskResultCallback {
        void onResult(TeamTask task);
    }

    public interface LogsCallback {
        void onResult(List<TeamTaskLog> logs);
    }

    /** Perubahan tugas oleh anggota LAIN (untuk notifikasi Fitur-07, Day 27). */
    public interface OtherChangesCallback {
        void onChanges(List<TeamTaskChange> changes);
    }

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public TeamTaskRepository() {
        this(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    public TeamTaskRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
    }

    private CollectionReference tasksRef(String teamId) {
        return db.collection("teams").document(teamId).collection("tasks");
    }

    @Nullable
    private String currentUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private String currentDisplayName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return "?";
        }
        String name = user.getDisplayName();
        return (name != null && !name.isEmpty()) ? name : user.getEmail();
    }

    private void writeActivityLog(String teamId, String taskId, String content) {
        Map<String, Object> log = new HashMap<>();
        log.put("logType", LogType.ACTIVITY);
        log.put("content", content);
        log.put("createdAt", System.currentTimeMillis());
        log.put("createdBy", currentDisplayName());
        tasksRef(teamId).document(taskId).collection("logs").add(log);
    }

    public void createTask(String teamId, TeamTask task, TaskCallback callback) {
        String uid = currentUid();
        task.createdBy = uid;
        task.updatedBy = uid;
        task.updatedAt = System.currentTimeMillis();
        tasksRef(teamId).add(task)
                .addOnSuccessListener(ref -> {
                    // Fitur-02: catat aktivitas otomatis dengan nama anggota.
                    writeActivityLog(teamId, ref.getId(), "Tugas dibuat");
                    callback.onResult(true, ref.getId(), null);
                })
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    public void updateTask(String teamId, TeamTask task, TaskCallback callback) {
        // Partial update: hanya field yang bisa diedit, agar createdBy/assignedTo/
        // createdAt tidak terhapus (berbeda dengan set() yang menimpa seluruh doc).
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", task.title);
        updates.put("description", task.description);
        updates.put("deadline", task.deadline);
        updates.put("priority", task.priority);
        updates.put("status", task.status);
        updates.put("updatedAt", System.currentTimeMillis());
        updates.put("updatedBy", currentUid());
        tasksRef(teamId).document(task.id).update(updates)
                .addOnSuccessListener(x -> {
                    writeActivityLog(teamId, task.id, "Tugas diperbarui");
                    callback.onResult(true, task.id, null);
                })
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    public void deleteTask(String teamId, String taskId, TaskCallback callback) {
        // Firestore tidak meng-cascade subcollection: hapus logs + task dalam
        // satu batch (client-side) agar tak ada log orphan (blueprint Bagian 6).
        DocumentReference taskRef = tasksRef(teamId).document(taskId);
        taskRef.collection("logs").get()
                .addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        batch.delete(d.getReference());
                    }
                    batch.delete(taskRef);
                    batch.commit()
                            .addOnSuccessListener(x -> callback.onResult(true, taskId, null))
                            .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    /**
     * Read realtime (Day 19): pasang snapshot listener pada koleksi tasks.
     * Mengembalikan {@link ListenerRegistration} agar pemanggil (ViewModel)
     * bisa melepasnya saat tidak dibutuhkan lagi — mencegah listener bocor.
     * Listener juga emit dari cache lokal (offline) lebih dulu.
     */
    public ListenerRegistration listenTasks(String teamId, TasksCallback callback) {
        return tasksRef(teamId)
                .orderBy("deadline", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }
                    List<TeamTask> tasks = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        TeamTask task = d.toObject(TeamTask.class);
                        if (task != null) {
                            task.id = d.getId();
                            tasks.add(task);
                        }
                    }
                    callback.onResult(tasks);
                });
    }

    /**
     * Listener khusus notifikasi (Day 27): laporkan perubahan tugas oleh anggota
     * LAIN selagi app hidup. Tiga penyaring:
     * <ol>
     *   <li>Snapshot pertama (muatan awal) dilewati — kalau tidak, semua tugas
     *       lama akan tampil sebagai "baru" tiap kali layar dibuka.</li>
     *   <li>{@code metadata.hasPendingWrites()} → tulisan sendiri yang belum
     *       dikonfirmasi server, jangan notif.</li>
     *   <li>{@code updatedBy == currentUid} → setelah dikonfirmasi server pun,
     *       perubahan milik sendiri tetap disaring.</li>
     * </ol>
     * Listener terpisah dari {@link #listenTasks} agar tanggung jawabnya jelas;
     * keduanya berbagi data cache Firestore yang sama (murah).
     */
    public ListenerRegistration listenOtherChanges(String teamId, OtherChangesCallback callback) {
        final boolean[] firstSnapshot = {true};
        return tasksRef(teamId).addSnapshotListener((snap, err) -> {
            if (err != null || snap == null) {
                return;
            }
            if (firstSnapshot[0]) {
                firstSnapshot[0] = false;
                return;
            }
            String uid = currentUid();
            List<TeamTaskChange> changes = new ArrayList<>();
            for (DocumentChange dc : snap.getDocumentChanges()) {
                DocumentSnapshot doc = dc.getDocument();
                if (doc.getMetadata().hasPendingWrites()) {
                    continue;
                }
                TeamTask task = doc.toObject(TeamTask.class);
                if (task == null) {
                    continue;
                }
                if (uid != null && uid.equals(task.updatedBy)) {
                    continue;
                }
                changes.add(new TeamTaskChange(task.title, mapChangeType(dc.getType())));
            }
            if (!changes.isEmpty()) {
                callback.onChanges(changes);
            }
        });
    }

    private static TeamTaskChange.Type mapChangeType(DocumentChange.Type type) {
        switch (type) {
            case ADDED:
                return TeamTaskChange.Type.ADDED;
            case REMOVED:
                return TeamTaskChange.Type.REMOVED;
            case MODIFIED:
            default:
                return TeamTaskChange.Type.MODIFIED;
        }
    }

    /** Baca sekali (non-realtime) — dipakai verifikasi Day 18. */
    public void loadTasksOnce(String teamId, TasksCallback callback) {
        tasksRef(teamId).get()
                .addOnSuccessListener(snap -> {
                    List<TeamTask> tasks = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        TeamTask task = d.toObject(TeamTask.class);
                        if (task != null) {
                            task.id = d.getId();
                            tasks.add(task);
                        }
                    }
                    callback.onResult(tasks);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // --- Detail tugas: observasi 1 dokumen + riwayat (Day 23) ---

    /** Observasi satu tugas realtime (untuk header layar detail). null = terhapus. */
    public ListenerRegistration observeTask(String teamId, String taskId, TaskResultCallback callback) {
        return tasksRef(teamId).document(taskId).addSnapshotListener((doc, err) -> {
            if (err != null || doc == null || !doc.exists()) {
                callback.onResult(null);
                return;
            }
            TeamTask task = doc.toObject(TeamTask.class);
            if (task != null) {
                task.id = doc.getId();
            }
            callback.onResult(task);
        });
    }

    /** Observasi riwayat (ACTIVITY + NOTE) sebuah tugas, terbaru di atas. */
    public ListenerRegistration observeLogs(String teamId, String taskId, LogsCallback callback) {
        return tasksRef(teamId).document(taskId).collection("logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }
                    List<TeamTaskLog> logs = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        TeamTaskLog log = d.toObject(TeamTaskLog.class);
                        if (log != null) {
                            log.id = d.getId();
                            logs.add(log);
                        }
                    }
                    callback.onResult(logs);
                });
    }

    /** Fitur-05: catatan manual (NOTE) dari user. */
    public void addNote(String teamId, String taskId, String content) {
        Map<String, Object> log = new HashMap<>();
        log.put("logType", LogType.NOTE);
        log.put("content", content);
        log.put("createdAt", System.currentTimeMillis());
        log.put("createdBy", currentDisplayName());
        tasksRef(teamId).document(taskId).collection("logs").add(log);
    }
}
