package com.example.taskreminder2.ui.team;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/** Menyuntik {@code teamId} & {@code taskId} ke {@link TeamTaskDetailViewModel}. */
public class TeamTaskDetailViewModelFactory implements ViewModelProvider.Factory {

    private final String teamId;
    private final String taskId;

    public TeamTaskDetailViewModelFactory(String teamId, String taskId) {
        this.teamId = teamId;
        this.taskId = taskId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeamTaskDetailViewModel.class)) {
            return (T) new TeamTaskDetailViewModel(teamId, taskId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
