package com.example.taskreminder2.data.repository;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Wrapper Firebase Authentication (Team Mode). Satu-satunya jalur resmi
 * ViewModel ke Firebase Auth — Activity tidak menyentuh Firebase langsung.
 *
 * <p>Constructor menerima {@link FirebaseAuth} agar bisa di-mock saat test.</p>
 */
public class AuthRepository {

    /** Callback hasil operasi async Firebase Auth. */
    public interface AuthCallback {
        void onResult(boolean success, @Nullable String errorMessage);
    }

    private final FirebaseAuth auth;

    public AuthRepository() {
        this(FirebaseAuth.getInstance());
    }

    public AuthRepository(FirebaseAuth auth) {
        this.auth = auth;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> callback.onResult(true, null))
                .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
    }

    public void register(String displayName, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null && displayName != null && !displayName.isEmpty()) {
                        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build();
                        // Selesaikan callback setelah displayName tersimpan.
                        user.updateProfile(req)
                                .addOnCompleteListener(t -> callback.onResult(true, null));
                    } else {
                        callback.onResult(true, null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(false, e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }
}
