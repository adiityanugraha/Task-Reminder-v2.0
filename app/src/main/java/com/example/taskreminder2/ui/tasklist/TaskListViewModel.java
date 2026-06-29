package com.example.taskreminder2.ui.tasklist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.repository.TaskRepository;
import com.example.taskreminder2.ui.TaskQuery;

import java.util.List;

/**
 * ViewModel layar daftar tugas (Personal Mode).
 *
 * <p>Daftar tugas yang diekspos bersifat dinamis (Fitur-03): sebuah trigger
 * {@link TaskQuery} di-{@code switchMap} ke sumber {@link LiveData} yang sesuai
 * (semua / cari judul / prioritas / terlambat). Mengubah filter cukup
 * mengganti nilai trigger — Room menyediakan LiveData baru, UI ikut otomatis.</p>
 */
public class TaskListViewModel extends ViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<TaskQuery> query = new MutableLiveData<>();
    private final LiveData<List<Task>> tasks;

    public TaskListViewModel(TaskRepository repository) {
        this.repository = repository;
        this.tasks = Transformations.switchMap(query, q -> {
            if (q.hasKeyword()) {
                return repository.searchByTitle(q.trimmedKeyword());
            }
            switch (q.filter) {
                case PRIORITY_HIGH:
                    return repository.filterByPriority(Task.PRIORITY_HIGH);
                case OVERDUE:
                    return repository.filterOverdue(System.currentTimeMillis());
                case ALL:
                default:
                    return repository.getAllTasks();
            }
        });
        query.setValue(new TaskQuery(TaskQuery.Filter.ALL, null));
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    /**
     * Memperbarui kriteria tampilan. Jika kata kunci tidak kosong, pencarian
     * judul diprioritaskan; selain itu memakai filter.
     */
    public void setQuery(TaskQuery query) {
        this.query.setValue(query);
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
