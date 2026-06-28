package com.example.taskreminder2.ui.team;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/** Menyuntik {@code teamId} ke {@link TeamTaskViewModel}. */
public class TeamTaskViewModelFactory implements ViewModelProvider.Factory {

    private final String teamId;

    public TeamTaskViewModelFactory(String teamId) {
        this.teamId = teamId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeamTaskViewModel.class)) {
            return (T) new TeamTaskViewModel(teamId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
