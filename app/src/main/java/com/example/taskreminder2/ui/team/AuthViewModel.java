package com.example.taskreminder2.ui.team;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskreminder2.data.repository.AuthRepository;

/**
 * ViewModel untuk login/register/logout (Fitur-04). Mengekspos state UI
 * (loading, sukses, error) sebagai LiveData. Menerima {@link AuthRepository}
 * lewat constructor agar bisa diuji dengan repo mock.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repo;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel() {
        this(new AuthRepository());
    }

    public AuthViewModel(AuthRepository repo) {
        this.repo = repo;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getSuccess() {
        return success;
    }

    public LiveData<String> getError() {
        return error;
    }

    public boolean isLoggedIn() {
        return repo.getCurrentUser() != null;
    }

    public String currentUserEmail() {
        return repo.getCurrentUser() != null ? repo.getCurrentUser().getEmail() : null;
    }

    public void login(String email, String password) {
        loading.setValue(true);
        repo.login(email, password, (ok, msg) -> {
            loading.setValue(false);
            if (ok) {
                success.setValue(true);
            } else {
                error.setValue(msg);
            }
        });
    }

    public void register(String displayName, String email, String password) {
        loading.setValue(true);
        repo.register(displayName, email, password, (ok, msg) -> {
            loading.setValue(false);
            if (ok) {
                success.setValue(true);
            } else {
                error.setValue(msg);
            }
        });
    }

    public void logout() {
        repo.logout();
    }
}
