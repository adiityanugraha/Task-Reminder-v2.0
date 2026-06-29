package com.example.taskreminder2.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.taskreminder2.ui.SearchFilterBinder;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Daftar tugas Team Mode — realtime via snapshot listener. FAB & klik item
 * membuka {@link TeamTaskFormActivity} (Fitur-01 Team). Menu "Kelola Team"
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
    private boolean filtering;

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
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        RecyclerView recycler = findViewById(R.id.recyclerTeamTasks);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        TeamTaskAdapter adapter = new TeamTaskAdapter(this);
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new TeamTaskViewModelFactory(team.id))
                .get(TeamTaskViewModel.class);

        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            boolean empty = tasks == null || tasks.isEmpty();
            textEmpty.setText(filtering ? R.string.empty_filtered : R.string.empty_team_tasks);
            textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        SearchFilterBinder.bind(editSearch, chipGroup, query -> {
            filtering = query.isFiltering();
            viewModel.setQuery(query);
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> TeamTaskFormActivity.start(this, team.id));
    }

    @Override
    public void onTaskClick(TeamTask task) {
        TeamTaskDetailActivity.start(this, team.id, task.id);
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
