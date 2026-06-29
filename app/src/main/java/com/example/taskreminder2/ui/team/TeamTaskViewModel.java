package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.repository.TeamTaskRepository;
import com.example.taskreminder2.util.OverdueChecker;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel daftar tugas Team Mode dengan pencarian & filter (Fitur-03).
 * Karena Team Mode tidak memakai Room, filter dilakukan DI MEMORI atas daftar
 * yang sudah diobserve dari snapshot listener — cepat & tetap jalan offline
 * (listener emit dari cache Firestore). Listener dilepas di {@link #onCleared()}.
 */
public class TeamTaskViewModel extends ViewModel {

    public enum Filter { ALL, PRIORITY_HIGH, OVERDUE }

    private static final class Query {
        final Filter filter;
        final String keyword;

        Query(Filter filter, String keyword) {
            this.filter = filter;
            this.keyword = keyword;
        }
    }

    private final TeamTaskRepository repo;
    private final String teamId;
    private final ListenerRegistration registration;

    private final MutableLiveData<List<TeamTask>> rawTasks = new MutableLiveData<>();
    private final MutableLiveData<Query> query = new MutableLiveData<>(new Query(Filter.ALL, null));
    private final MediatorLiveData<List<TeamTask>> filteredTasks = new MediatorLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public TeamTaskViewModel(String teamId) {
        this(new TeamTaskRepository(), teamId);
    }

    public TeamTaskViewModel(TeamTaskRepository repo, String teamId) {
        this.repo = repo;
        this.teamId = teamId;
        this.registration = repo.listenTasks(teamId, rawTasks::setValue);
        filteredTasks.addSource(rawTasks, t -> recompute());
        filteredTasks.addSource(query, q -> recompute());
    }

    public LiveData<List<TeamTask>> getTasks() {
        return filteredTasks;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void setQuery(Filter filter, String keyword) {
        query.setValue(new Query(filter, keyword));
    }

    public void deleteTask(TeamTask task) {
        repo.deleteTask(teamId, task.id, (ok, id, err) -> {
            if (!ok) {
                message.setValue("Gagal: " + err);
            }
        });
    }

    private void recompute() {
        filteredTasks.setValue(applyFilter(rawTasks.getValue(), query.getValue()));
    }

    /** Filter di memori: keyword (judul) diprioritaskan; selain itu pakai filter. */
    private static List<TeamTask> applyFilter(List<TeamTask> all, Query q) {
        List<TeamTask> result = new ArrayList<>();
        if (all == null) {
            return result;
        }
        Query query = q != null ? q : new Query(Filter.ALL, null);
        String kw = query.keyword == null ? "" : query.keyword.trim().toLowerCase();
        long now = System.currentTimeMillis();
        for (TeamTask t : all) {
            if (!kw.isEmpty()) {
                if (t.title == null || !t.title.toLowerCase().contains(kw)) {
                    continue;
                }
            } else if (query.filter == Filter.PRIORITY_HIGH) {
                if (t.priority != Task.PRIORITY_HIGH) {
                    continue;
                }
            } else if (query.filter == Filter.OVERDUE) {
                if (!OverdueChecker.isOverdue(t.deadline, t.status, now)) {
                    continue;
                }
            }
            result.add(t);
        }
        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) {
            registration.remove();
        }
    }
}
