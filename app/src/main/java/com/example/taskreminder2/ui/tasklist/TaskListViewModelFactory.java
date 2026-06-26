package com.example.taskreminder2.ui.tasklist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.data.repository.TaskRepository;

/**
 * Factory yang merakit {@link TaskListViewModel} beserta dependency-nya
 * ({@link TaskRepository}) dari {@link Application} context.
 *
 * <p>Dipakai Activity (Day 5) saat memanggil {@code new ViewModelProvider(
 * this, new TaskListViewModelFactory(getApplication()))}. Memisahkan perakitan
 * dependency dari ViewModel inilah yang membuat ViewModel tetap mudah ditest.</p>
 */
public class TaskListViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public TaskListViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TaskListViewModel.class)) {
            return (T) new TaskListViewModel(new TaskRepository(application));
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
