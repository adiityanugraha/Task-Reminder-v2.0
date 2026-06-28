package com.example.taskreminder2.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.example.taskreminder2.ui.TaskViewBinder;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Detail tugas Team Mode (Day 23): info tugas (realtime) + Riwayat (Fitur-02
 * aktivitas otomatis & Fitur-05 catatan manual). Edit/Hapus lewat menu.
 * Memakai ulang layout {@code activity_task_detail} (identik dengan Personal).
 */
public class TeamTaskDetailActivity extends BaseToolbarActivity {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_TASK_ID = "extra_task_id";

    public static void start(Context context, String teamId, String taskId) {
        Intent intent = new Intent(context, TeamTaskDetailActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startActivity(intent);
    }

    private TeamTaskDetailViewModel viewModel;
    private String teamId;
    private TeamTask currentTask;

    private TextView textTitle;
    private TextView textPriority;
    private TextView textStatus;
    private TextView textDeadline;
    private TextView textDescription;
    private TextView textLogsEmpty;
    private int defaultDeadlineColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        setupToolbar(0, true);

        teamId = getIntent().getStringExtra(EXTRA_TEAM_ID);
        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);

        textTitle = findViewById(R.id.textDetailTitle);
        textPriority = findViewById(R.id.textDetailPriority);
        textStatus = findViewById(R.id.textDetailStatus);
        textDeadline = findViewById(R.id.textDetailDeadline);
        textDescription = findViewById(R.id.textDetailDescription);
        textLogsEmpty = findViewById(R.id.textLogsEmpty);
        defaultDeadlineColor = textDeadline.getCurrentTextColor();

        RecyclerView recyclerLogs = findViewById(R.id.recyclerLogs);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        TeamTaskLogAdapter logAdapter = new TeamTaskLogAdapter();
        recyclerLogs.setAdapter(logAdapter);

        TextInputEditText editNote = findViewById(R.id.editNote);
        MaterialButton buttonAddNote = findViewById(R.id.buttonAddNote);
        buttonAddNote.setOnClickListener(v -> addNote(editNote));

        viewModel = new ViewModelProvider(
                this, new TeamTaskDetailViewModelFactory(teamId, taskId))
                .get(TeamTaskDetailViewModel.class);

        viewModel.getTask().observe(this, task -> {
            if (task == null) {
                finish(); // tugas dihapus
                return;
            }
            currentTask = task;
            bindTask(task);
        });
        viewModel.getLogs().observe(this, logs -> {
            logAdapter.submitList(logs);
            textLogsEmpty.setVisibility(logs == null || logs.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
    }

    private void bindTask(TeamTask task) {
        setTitle(task.title);
        textTitle.setText(task.title);
        textPriority.setVisibility(task.priority == Task.PRIORITY_HIGH
                ? View.VISIBLE : View.GONE);
        textStatus.setText(getString(R.string.detail_status, TaskStatus.label(task.status)));
        TaskViewBinder.bindDeadline(textDeadline, task.deadline, task.status, defaultDeadlineColor);
        if (task.description == null || task.description.trim().isEmpty()) {
            textDescription.setText(R.string.detail_no_description);
        } else {
            textDescription.setText(task.description);
        }
    }

    private void addNote(TextInputEditText editNote) {
        String content = editNote.getText() == null ? "" : editNote.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            editNote.setError(getString(R.string.error_note_empty));
            return;
        }
        viewModel.addNote(content);
        editNote.setText("");
        editNote.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editNote.getWindowToken(), 0);
        }
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_team_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            if (currentTask != null) {
                TeamTaskFormActivity.start(this, teamId, currentTask);
            }
            return true;
        }
        if (id == R.id.action_delete) {
            if (currentTask != null) {
                viewModel.delete();
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
