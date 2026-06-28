package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.model.Team;
import com.example.taskreminder2.data.repository.TeamRepository;

import java.util.List;

/**
 * ViewModel TeamHome (Fitur-04): buat/join team + daftar team milik user.
 * Repository injectable untuk test.
 */
public class TeamViewModel extends ViewModel {

    private final TeamRepository repo;
    private final MutableLiveData<List<Team>> teams = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public TeamViewModel() {
        this(new TeamRepository());
    }

    public TeamViewModel(TeamRepository repo) {
        this.repo = repo;
    }

    public LiveData<List<Team>> getTeams() {
        return teams;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadTeams() {
        repo.loadUserTeams(teams::setValue);
    }

    public void createTeam(String name) {
        loading.setValue(true);
        repo.createTeam(name, (ok, info, err) -> {
            loading.setValue(false);
            if (ok) {
                message.setValue("Team dibuat. Kode undang: " + info);
                loadTeams();
            } else {
                message.setValue("Gagal: " + err);
            }
        });
    }

    public void joinTeam(String code) {
        loading.setValue(true);
        repo.joinTeamByCode(code, (ok, info, err) -> {
            loading.setValue(false);
            if (ok) {
                message.setValue("Berhasil gabung team: " + info);
                loadTeams();
            } else {
                message.setValue("Gagal: " + err);
            }
        });
    }
}
