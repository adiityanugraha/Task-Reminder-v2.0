package com.example.taskreminder2.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.model.Team;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Daftar tugas Team Mode (Day 19) — realtime via snapshot listener. Quick-add
 * (judul saja); form lengkap + badge/overdue di Day 22. Menu "Kelola Team"
 * membuka {@link ManageTeamActivity}.
 */
public class TeamTasksActivity extends BaseToolbarActivity
        implements TeamTaskAdapter.OnTaskInteractionListener {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_TEAM_NAME = "extra_team_name";
    private static final String EXTRA_TEAM_CODE = "extra_team_code";
    private static final String EXTRA_OWNER_ID = "extra_owner_id";

    public static void start(Context context, Team team) {
        Intent intent = new Intent(context, TeamTasksActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, team.id);
        intent.putExtra(EXTRA_TEAM_NAME, team.name);
        intent.putExtra(EXTRA_TEAM_CODE, team.inviteCode);
        intent.putExtra(EXTRA_OWNER_ID, team.ownerId);
        context.startActivity(intent);
    }

    private TeamTaskViewModel viewModel;
    private Team team;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_tasks);
        setupToolbar(0, true);

        team = new Team(
                getIntent().getStringExtra(EXTRA_TEAM_ID),
                getIntent().getStringExtra(EXTRA_TEAM_NAME),
                getIntent().getStringExtra(EXTRA_TEAM_CODE),
                getIntent().getStringExtra(EXTRA_OWNER_ID));
        setTitle(team.name);

        textEmpty = findViewById(R.id.textEmpty);
        RecyclerView recycler = findViewById(R.id.recyclerTeamTasks);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        TeamTaskAdapter adapter = new TeamTaskAdapter(this);
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new TeamTaskViewModelFactory(team.id))
                .get(TeamTaskViewModel.class);

        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            textEmpty.setVisibility(tasks == null || tasks.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null, false);
        TextInputLayout inputLayout = view.findViewById(R.id.inputLayout);
        TextInputEditText editInput = view.findViewById(R.id.editInput);
        inputLayout.setHint(R.string.hint_title);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_add_team_task_title)
                .setView(view)
                .setPositiveButton(R.string.button_save, (dialog, which) -> {
                    String title = editInput.getText() == null ? "" : editInput.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(this, R.string.error_title_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.createTask(title);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onTaskLongClick(TeamTask task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> viewModel.deleteTask(task))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_manage_team) {
            ManageTeamActivity.start(this, team);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
