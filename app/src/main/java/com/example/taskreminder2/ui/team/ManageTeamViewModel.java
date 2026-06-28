package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.model.Member;
import com.example.taskreminder2.data.repository.TeamRepository;

import java.util.List;

/**
 * ViewModel layar Kelola Team (Fitur-04, Day 17). Memuat anggota dan
 * menjalankan aksi owner (keluarkan member, hapus team). Pengecekan role
 * (owner vs member) dilakukan di sini/Activity sebelum mengizinkan aksi.
 */
public class ManageTeamViewModel extends ViewModel {

    private final TeamRepository repo;
    private final MutableLiveData<List<Member>> members = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> teamDeleted = new MutableLiveData<>();

    public ManageTeamViewModel() {
        this(new TeamRepository());
    }

    public ManageTeamViewModel(TeamRepository repo) {
        this.repo = repo;
    }

    public LiveData<List<Member>> getMembers() {
        return members;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getTeamDeleted() {
        return teamDeleted;
    }

    public String currentUid() {
        return repo.currentUid();
    }

    public boolean isOwner(String ownerId) {
        String uid = repo.currentUid();
        return uid != null && uid.equals(ownerId);
    }

    public void loadMembers(String teamId) {
        repo.loadMembers(teamId, members::setValue);
    }

    public void removeMember(String teamId, Member member) {
        repo.removeMember(teamId, member.uid, (ok, err) -> {
            if (ok) {
                message.setValue(member.displayName + " dikeluarkan");
                loadMembers(teamId);
            } else {
                message.setValue("Gagal: " + err);
            }
        });
    }

    public void deleteTeam(String teamId) {
        repo.deleteTeam(teamId, (ok, err) -> {
            if (ok) {
                teamDeleted.setValue(true);
            } else {
                message.setValue("Gagal: " + err);
            }
        });
    }
}
