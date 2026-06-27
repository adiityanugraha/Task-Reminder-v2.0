package com.example.taskreminder2.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskreminder2.data.local.AppDatabase;
import com.example.taskreminder2.data.local.dao.TaskDao;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Satu-satunya jalur resmi akses data Task (Personal Mode).
 *
 * <p>Operasi tulis (insert/update/delete) dijalankan di background thread
 * lewat {@link ExecutorService}. Setiap insert/update juga mencatat baris
 * riwayat lewat {@link TaskLogRepository} (Fitur-02) — pemanggilan ini berada
 * di SATU layer yang jelas (Repository), bukan tersebar seperti Singleton
 * lama yang saling memanggil tersembunyi.</p>
 */
public class TaskRepository {

    private static final ExecutorService SHARED_IO = Executors.newFixedThreadPool(4);

    private final TaskDao taskDao;
    private final TaskLogRepository logRepository;
    private final ExecutorService io;
    private final LiveData<List<Task>> allTasks;

    /** Dipakai produksi: bangun dari Application context. */
    public TaskRepository(Application application) {
        this(AppDatabase.getInstance(application).taskDao(),
                new TaskLogRepository(application),
                SHARED_IO);
    }

    /** Dipakai unit test: injeksi DAO (mock), log repo (mock) & executor. */
    public TaskRepository(TaskDao taskDao, TaskLogRepository logRepository, ExecutorService io) {
        this.taskDao = taskDao;
        this.logRepository = logRepository;
        this.io = io;
        this.allTasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<Task> getTaskById(int id) {
        return taskDao.getById(id);
    }

    // --- Fitur-03: pencarian & filter (passthrough ke DAO) ---

    public LiveData<List<Task>> searchByTitle(String keyword) {
        return taskDao.searchByTitle(keyword);
    }

    public LiveData<List<Task>> filterByPriority(int priority) {
        return taskDao.filterByPriority(priority);
    }

    public LiveData<List<Task>> filterOverdue(long now) {
        return taskDao.filterOverdue(now);
    }

    public void insert(Task task) {
        io.execute(() -> {
            long id = taskDao.insert(task);
            logRepository.logActivity((int) id, "Tugas dibuat");
        });
    }

    public void update(Task task) {
        io.execute(() -> {
            Task before = taskDao.getByIdSync(task.id);
            taskDao.update(task);
            logRepository.logActivity(task.id, buildUpdateLog(before, task));
        });
    }

    public void delete(Task task) {
        // Tidak perlu log: log milik task ikut terhapus via ON DELETE CASCADE.
        io.execute(() -> taskDao.delete(task));
    }

    /**
     * Menyusun pesan log perubahan berdasarkan beda field lama vs baru.
     * Murni Java (tanpa dependency Android) agar mudah di-unit-test.
     */
    static String buildUpdateLog(Task before, Task after) {
        if (before == null) {
            return "Tugas diperbarui";
        }
        List<String> changes = new ArrayList<>();
        if (!textEquals(before.title, after.title)) {
            changes.add("judul diubah");
        }
        if (!textEquals(before.description, after.description)) {
            changes.add("deskripsi diubah");
        }
        if (before.deadline != after.deadline) {
            changes.add("deadline diubah");
        }
        if (!textEquals(before.status, after.status)) {
            changes.add("status diubah menjadi " + TaskStatus.label(after.status));
        }
        if (before.priority != after.priority) {
            changes.add("prioritas diubah menjadi " + (after.priority == 1 ? "Tinggi" : "Normal"));
        }
        if (changes.isEmpty()) {
            return "Tugas diperbarui";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < changes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(changes.get(i));
        }
        // Kapitalkan huruf pertama.
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private static boolean textEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
