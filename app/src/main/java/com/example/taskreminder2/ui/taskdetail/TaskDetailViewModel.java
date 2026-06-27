package com.example.taskreminder2.ui.taskdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.local.entity.TaskLog;
import com.example.taskreminder2.data.repository.TaskLogRepository;
import com.example.taskreminder2.data.repository.TaskRepository;

import java.util.List;

/**
 * ViewModel layar detail tugas. Mengekspos dua aliran reaktif untuk satu
 * {@code taskId}: data tugas (auto-refresh setelah edit) dan riwayatnya
 * (Fitur-02). Day 10 menambah penulisan catatan manual (Fitur-05).
 */
public class TaskDetailViewModel extends ViewModel {

    private final TaskRepository taskRepository;
    private final TaskLogRepository logRepository;
    private final LiveData<Task> task;
    private final LiveData<List<TaskLog>> logs;

    public TaskDetailViewModel(TaskRepository taskRepository,
                               TaskLogRepository logRepository,
                               int taskId) {
        this.taskRepository = taskRepository;
        this.logRepository = logRepository;
        this.task = taskRepository.getTaskById(taskId);
        this.logs = logRepository.getLogsForTask(taskId);
    }

    public LiveData<Task> getTask() {
        return task;
    }

    public LiveData<List<TaskLog>> getLogs() {
        return logs;
    }

    public void delete(Task task) {
        taskRepository.delete(task);
    }
}
