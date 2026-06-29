package com.example.taskreminder2.ui.team;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.notification.TeamSyncWorker;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Beranda Team Mode (Fitur-04, Day 16): buat team baru / gabung lewat kode,
 * dan menampilkan daftar team milik user. Hanya dicapai saat sudah login.
 */
public class TeamHomeActivity extends BaseToolbarActivity implements TeamAdapter.OnTeamClickListener {

    /** Callback input dialog (hindari java.util.function.Consumer; minSdk 23). */
    private interface InputListener {
        void onInput(String value);
    }

    private AuthViewModel authViewModel;
    private TeamViewModel teamViewModel;
    private TextView textEmptyTeams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_home);
        setupToolbar(R.string.title_team_home, true);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        if (!authViewModel.isLoggedIn()) {
            finish();
            return;
        }

        // Masuk Team Mode → aktifkan polling notif saat app tertutup (Day 28).
        // Idempoten, jadi aman dipanggil tiap kali layar ini dibuka.
        TeamSyncWorker.schedule(this);

        TextView textLoggedInAs = findViewById(R.id.textLoggedInAs);
        textLoggedInAs.setText(getString(R.string.team_logged_in_as, authViewModel.currentUserEmail()));

        textEmptyTeams = findViewById(R.id.textEmptyTeams);
        RecyclerView recycler = findViewById(R.id.recyclerTeams);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        TeamAdapter adapter = new TeamAdapter(this);
        recycler.setAdapter(adapter);

        teamViewModel.getTeams().observe(this, teams -> {
            adapter.submitList(teams);
            textEmptyTeams.setVisibility(teams == null || teams.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
        teamViewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        MaterialButton buttonCreate = findViewById(R.id.buttonCreateTeam);
        MaterialButton buttonJoin = findViewById(R.id.buttonJoinTeam);
        buttonCreate.setOnClickListener(v -> showInputDialog(
                R.string.dialog_create_team_title, R.string.hint_team_name,
                R.string.action_create, R.string.error_team_name_required,
                teamViewModel::createTeam));
        buttonJoin.setOnClickListener(v -> showInputDialog(
                R.string.dialog_join_team_title, R.string.hint_invite_code,
                R.string.action_join, R.string.error_code_required,
                code -> teamViewModel.joinTeam(code.toUpperCase())));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Muat ulang daftar team (mis. kembali dari layar lain).
        if (authViewModel.isLoggedIn()) {
            teamViewModel.loadTeams();
        }
    }

    private void showInputDialog(@StringRes int title, @StringRes int hint,
                                 @StringRes int positive, @StringRes int emptyError,
                                 InputListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null, false);
        TextInputLayout inputLayout = view.findViewById(R.id.inputLayout);
        TextInputEditText editInput = view.findViewById(R.id.editInput);
        inputLayout.setHint(hint);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positive, (dialog, which) -> {
                    String value = editInput.getText() == null ? "" : editInput.getText().toString().trim();
                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(this, emptyError, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onInput(value);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onTeamClick(com.example.taskreminder2.data.model.Team team) {
        // Day 19: buka daftar tugas team. Kelola Team diakses dari menu di sana.
        TeamTasksActivity.start(this, team);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Logout → hentikan polling Team (tak ada user yang dipantau).
            TeamSyncWorker.cancel(this);
            authViewModel.logout();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
