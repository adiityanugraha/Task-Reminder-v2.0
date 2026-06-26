package com.example.taskreminder2.ui.tasklist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.repository.TaskRepository;

import java.util.List;

/**
 * ViewModel layar daftar tugas (Personal Mode).
 *
 * <p>Menerima {@link TaskRepository} lewat constructor (injeksi) sehingga
 * bisa diuji dengan Repository mock tanpa Activity/emulator. Mengekspos
 * {@code LiveData<List<Task>>} ke UI; UI cukup mengamati, tidak tahu sumber
 * datanya Room atau bukan.</p>
 */
public class TaskListViewModel extends ViewModel {

    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;

    public TaskListViewModel(TaskRepository repository) {
        this.repository = repository;
        this.allTasks = repository.getAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }
}
