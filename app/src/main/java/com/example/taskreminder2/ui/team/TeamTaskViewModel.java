package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.repository.TeamTaskRepository;
import com.example.taskreminder2.util.TaskStatus;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * ViewModel daftar tugas Team Mode (Day 19). Memegang snapshot listener
 * realtime dan melepasnya di {@link #onCleared()} agar tidak bocor. Pola MVVM
 * tetap utuh: Activity → ViewModel → Repository (Firestore di baliknya).
 */
public class TeamTaskViewModel extends ViewModel {

    private final TeamTaskRepository repo;
    private final String teamId;
    private final MutableLiveData<List<TeamTask>> tasks = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final ListenerRegistration registration;

    public TeamTaskViewModel(String teamId) {
        this(new TeamTaskRepository(), teamId);
    }

    public TeamTaskViewModel(TeamTaskRepository repo, String teamId) {
        this.repo = repo;
        this.teamId = teamId;
        // Listener emit dari cache lokal lebih dulu lalu sinkron dengan server.
        registration = repo.listenTasks(teamId, tasks::setValue);
    }

    public LiveData<List<TeamTask>> getTasks() {
        return tasks;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    /** Day 19: quick-add (judul saja). Form lengkap di Day 22. */
    public void createTask(String title) {
        TeamTask task = new TeamTask();
        task.title = title;
        task.description = "";
        task.deadline = 0;
        task.priority = 0;
        task.status = TaskStatus.NOT_STARTED;
        repo.createTask(teamId, task, (ok, id, err) -> {
            if (!ok) {
                message.setValue("Gagal: " + err);
            }
        });
    }

    public void deleteTask(TeamTask task) {
        repo.deleteTask(teamId, task.id, (ok, id, err) -> {
            if (!ok) {
                message.setValue("Gagal: " + err);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) {
            registration.remove();
        }
    }
}
