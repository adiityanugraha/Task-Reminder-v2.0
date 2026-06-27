package com.example.taskreminder2.ui.taskdetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.data.repository.TaskLogRepository;
import com.example.taskreminder2.data.repository.TaskRepository;

/** Merakit {@link TaskDetailViewModel} dengan Repository + taskId. */
public class TaskDetailViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final int taskId;

    public TaskDetailViewModelFactory(Application application, int taskId) {
        this.application = application;
        this.taskId = taskId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TaskDetailViewModel.class)) {
            return (T) new TaskDetailViewModel(
                    new TaskRepository(application),
                    new TaskLogRepository(application),
                    taskId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
