package com.example.taskreminder2.ui.team;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Gerbang Team Mode (Fitur-04): kalau sudah ada sesi, langsung ke TeamHome;
 * kalau belum, tampilkan form login. Sukses → TeamHome.
 */
public class LoginActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private CircularProgressIndicator progress;
    private MaterialButton buttonLogin;

    /** Register di-launch via result; sukses (RESULT_OK) → lanjut TeamHome. */
    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            goToTeamHome();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_login);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Sudah login → lewati form, langsung ke TeamHome.
        if (viewModel.isLoggedIn()) {
            goToTeamHome();
            return;
        }

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        progress = findViewById(R.id.progress);
        buttonLogin = findViewById(R.id.buttonLogin);
        MaterialButton buttonGoRegister = findViewById(R.id.buttonGoRegister);

        viewModel.getLoading().observe(this, this::setLoading);
        viewModel.getSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                goToTeamHome();
            }
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonGoRegister.setOnClickListener(v ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = text(editEmail);
        String password = text(editPassword);
        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (password.length() < 6) {
            editPassword.setError(getString(R.string.error_password_short));
            return;
        }
        viewModel.login(email, password);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!loading);
    }

    private void goToTeamHome() {
        startActivity(new Intent(this, TeamHomeActivity.class));
        finish();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
