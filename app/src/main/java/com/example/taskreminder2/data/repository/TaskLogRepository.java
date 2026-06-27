package com.example.taskreminder2.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.taskreminder2.data.local.AppDatabase;
import com.example.taskreminder2.data.local.dao.TaskLogDao;
import com.example.taskreminder2.data.local.entity.TaskLog;
import com.example.taskreminder2.util.LogType;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Jalur resmi akses {@code task_logs} (Fitur-02 & Fitur-05).
 *
 * <p>Penulisan di background thread. Day 9 akan menyambungkan
 * {@link #logActivity(int, String)} ke {@code TaskRepository} (log otomatis),
 * dan Day 10 memakai {@link #insertNote(int, String)} dari input user.</p>
 */
public class TaskLogRepository {

    private static final ExecutorService SHARED_IO = Executors.newFixedThreadPool(2);

    private final TaskLogDao dao;
    private final ExecutorService io;

    public TaskLogRepository(Application application) {
        this(AppDatabase.getInstance(application).taskLogDao(), SHARED_IO);
    }

    /** Dipakai unit test: injeksi DAO (mock) & executor. */
    public TaskLogRepository(TaskLogDao dao, ExecutorService io) {
        this.dao = dao;
        this.io = io;
    }

    public LiveData<List<TaskLog>> getLogsForTask(int taskId) {
        return dao.getLogsForTask(taskId);
    }

    /** Catatan aktivitas otomatis sistem (Fitur-02). */
    public void logActivity(int taskId, String content) {
        insert(taskId, LogType.ACTIVITY, content);
    }

    /** Catatan manual user (Fitur-05). */
    public void insertNote(int taskId, String content) {
        insert(taskId, LogType.NOTE, content);
    }

    private void insert(int taskId, String logType, String content) {
        io.execute(() -> {
            TaskLog log = new TaskLog();
            log.taskId = taskId;
            log.logType = logType;
            log.content = content;
            log.createdAt = System.currentTimeMillis();
            dao.insert(log);
        });
    }
}
