package com.example.taskreminder2.data.repository;

import androidx.annotation.Nullable;

import com.example.taskreminder2.data.model.TeamTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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

    public void createTask(String teamId, TeamTask task, TaskCallback callback) {
        String uid = currentUid();
        task.createdBy = uid;
        task.updatedBy = uid;
        task.updatedAt = System.currentTimeMillis();
        tasksRef(teamId).add(task)
                .addOnSuccessListener(ref -> callback.onResult(true, ref.getId(), null))
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    public void updateTask(String teamId, TeamTask task, TaskCallback callback) {
        task.updatedBy = currentUid();
        task.updatedAt = System.currentTimeMillis();
        tasksRef(teamId).document(task.id).set(task)
                .addOnSuccessListener(x -> callback.onResult(true, task.id, null))
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    public void deleteTask(String teamId, String taskId, TaskCallback callback) {
        // Day 23/27: saat subcollection logs sudah ada, hapus juga lewat batch.
        tasksRef(teamId).document(taskId).delete()
                .addOnSuccessListener(x -> callback.onResult(true, taskId, null))
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    /** Baca sekali (non-realtime) — dipakai verifikasi Day 18. Day 19 menambah listener. */
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
}
