package com.example.taskreminder2.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskreminder2.data.local.AppDatabase;
import com.example.taskreminder2.data.local.dao.TaskDao;
import com.example.taskreminder2.data.local.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Satu-satunya jalur resmi akses data Task (Personal Mode).
 *
 * <p>Operasi tulis (insert/update/delete) dijalankan di background thread
 * lewat {@link ExecutorService} — Room melarang operasi blocking di main
 * thread. Operasi baca diekspos sebagai {@link LiveData}, yang Room update
 * otomatis di thread yang benar.</p>
 *
 * <p>ViewModel adalah satu-satunya yang boleh memanggil Repository; Activity
 * tidak pernah menyentuh kelas ini langsung.</p>
 */
public class TaskRepository {

    /** Pool background bersama untuk semua operasi tulis DB Personal Mode. */
    private static final ExecutorService SHARED_IO = Executors.newFixedThreadPool(4);

    private final TaskDao taskDao;
    private final ExecutorService io;
    private final LiveData<List<Task>> allTasks;

    /** Dipakai produksi: bangun dari Application context. */
    public TaskRepository(Application application) {
        this(AppDatabase.getInstance(application).taskDao(), SHARED_IO);
    }

    /** Dipakai unit test: injeksi DAO (mock) & executor (mis. sinkron). */
    public TaskRepository(TaskDao taskDao, ExecutorService io) {
        this.taskDao = taskDao;
        this.io = io;
        this.allTasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void insert(Task task) {
        io.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        io.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        io.execute(() -> taskDao.delete(task));
    }
}
