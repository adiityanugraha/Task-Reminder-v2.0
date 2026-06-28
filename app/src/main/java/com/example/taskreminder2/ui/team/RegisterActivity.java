package com.example.taskreminder2.ui.team;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Pendaftaran akun (Fitur-04). Firebase otomatis login setelah register,
 * jadi sukses → langsung TeamHome.
 */
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText editName;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private CircularProgressIndicator progress;
    private MaterialButton buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_register);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        progress = findViewById(R.id.progress);
        buttonRegister = findViewById(R.id.buttonRegister);
        MaterialButton buttonGoLogin = findViewById(R.id.buttonGoLogin);

        viewModel.getLoading().observe(this, this::setLoading);
        viewModel.getSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                // Beri tahu LoginActivity agar dia yang lanjut ke TeamHome
                // (supaya kedua layar auth bersih dari back stack).
                setResult(RESULT_OK);
                finish();
            }
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        buttonRegister.setOnClickListener(v -> attemptRegister());
        buttonGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = text(editName);
        String email = text(editEmail);
        String password = text(editPassword);
        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_name_required));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (password.length() < 6) {
            editPassword.setError(getString(R.string.error_password_short));
            return;
        }
        viewModel.register(name, email, password);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!loading);
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
