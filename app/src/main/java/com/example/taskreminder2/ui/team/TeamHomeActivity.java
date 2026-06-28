package com.example.taskreminder2.ui.team;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import android.widget.TextView;

/**
 * Beranda Team Mode (placeholder Day 15). Hanya dicapai saat sudah login.
 * Day 16 akan mengisi: buat team / join via kode. Untuk sekarang menampilkan
 * akun yang masuk + tombol keluar.
 */
public class TeamHomeActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_team_home);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Pengaman: kalau entah bagaimana belum login, kembali.
        if (!viewModel.isLoggedIn()) {
            finish();
            return;
        }

        TextView textLoggedInAs = findViewById(R.id.textLoggedInAs);
        textLoggedInAs.setText(getString(R.string.team_logged_in_as, viewModel.currentUserEmail()));

        MaterialButton buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            viewModel.logout();
            finish(); // kembali ke Personal Mode (TaskListActivity)
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
