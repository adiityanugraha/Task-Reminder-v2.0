package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.repository.TeamTaskRepository;

/**
 * ViewModel form tambah/edit tugas Team Mode (Day 22). Hanya create/update —
 * tanpa snapshot listener (daftar realtime ditangani {@link TeamTaskViewModel}).
 */
public class TeamTaskFormViewModel extends ViewModel {

    private final TeamTaskRepository repo;
    private final String teamId;
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>();

    public TeamTaskFormViewModel(String teamId) {
        this(new TeamTaskRepository(), teamId);
    }

    public TeamTaskFormViewModel(TeamTaskRepository repo, String teamId) {
        this.repo = repo;
        this.teamId = teamId;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    /** id kosong → create; id terisi → update. */
    public void save(TeamTask task) {
        TeamTaskRepository.TaskCallback cb = (ok, id, err) -> {
            if (ok) {
                saved.setValue(true);
            } else {
                message.setValue("Gagal: " + err);
            }
        };
        if (task.id == null || task.id.isEmpty()) {
            repo.createTask(teamId, task, cb);
        } else {
            repo.updateTask(teamId, task, cb);
        }
    }
}
