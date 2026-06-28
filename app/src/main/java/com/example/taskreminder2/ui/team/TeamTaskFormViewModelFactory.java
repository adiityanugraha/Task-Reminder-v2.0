package com.example.taskreminder2.ui.team;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/** Menyuntik {@code teamId} ke {@link TeamTaskFormViewModel}. */
public class TeamTaskFormViewModelFactory implements ViewModelProvider.Factory {

    private final String teamId;

    public TeamTaskFormViewModelFactory(String teamId) {
        this.teamId = teamId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TeamTaskFormViewModel.class)) {
            return (T) new TeamTaskFormViewModel(teamId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
