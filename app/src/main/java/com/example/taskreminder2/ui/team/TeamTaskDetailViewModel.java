package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.model.TeamTaskLog;
import com.example.taskreminder2.data.repository.TeamTaskRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * ViewModel detail tugas Team Mode (Day 23): observasi realtime data tugas +
 * riwayat (Fitur-02/05), serta tambah catatan & hapus. Dua listener dilepas
 * di {@link #onCleared()} agar tidak bocor.
 */
public class TeamTaskDetailViewModel extends ViewModel {

    private final TeamTaskRepository repo;
    private final String teamId;
    private final String taskId;
    private final MutableLiveData<TeamTask> task = new MutableLiveData<>();
    private final MutableLiveData<List<TeamTaskLog>> logs = new MutableLiveData<>();
    private final ListenerRegistration taskRegistration;
    private final ListenerRegistration logsRegistration;

    public TeamTaskDetailViewModel(String teamId, String taskId) {
        this(new TeamTaskRepository(), teamId, taskId);
    }

    public TeamTaskDetailViewModel(TeamTaskRepository repo, String teamId, String taskId) {
        this.repo = repo;
        this.teamId = teamId;
        this.taskId = taskId;
        this.taskRegistration = repo.observeTask(teamId, taskId, task::setValue);
        this.logsRegistration = repo.observeLogs(teamId, taskId, logs::setValue);
    }

    public LiveData<TeamTask> getTask() {
        return task;
    }

    public LiveData<List<TeamTaskLog>> getLogs() {
        return logs;
    }

    public void addNote(String content) {
        repo.addNote(teamId, taskId, content);
    }

    public void delete() {
        repo.deleteTask(teamId, taskId, (ok, id, err) -> {
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (taskRegistration != null) {
            taskRegistration.remove();
        }
        if (logsRegistration != null) {
            logsRegistration.remove();
        }
    }
}
