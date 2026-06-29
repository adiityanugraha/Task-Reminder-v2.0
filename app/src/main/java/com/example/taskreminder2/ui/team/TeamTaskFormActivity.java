package com.example.taskreminder2.ui.team;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * Form tambah/edit tugas Team Mode (Fitur-01 Team). Memakai ulang layout
 * {@code activity_task_form} (field identik dengan Personal Mode). Mode edit
 * ditentukan dari ada-tidaknya {@link #EXTRA_TASK_ID}.
 */
public class TeamTaskFormActivity extends BaseToolbarActivity {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DESCRIPTION = "extra_description";
    private static final String EXTRA_DEADLINE = "extra_deadline";
    private static final String EXTRA_PRIORITY = "extra_priority";
    private static final String EXTRA_STATUS = "extra_status";

    public static void start(Context context, String teamId) {
        Intent intent = new Intent(context, TeamTaskFormActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        context.startActivity(intent);
    }

    public static void start(Context context, String teamId, TeamTask task) {
        Intent intent = new Intent(context, TeamTaskFormActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_TASK_ID, task.id);
        intent.putExtra(EXTRA_TITLE, task.title);
        intent.putExtra(EXTRA_DESCRIPTION, task.description);
        intent.putExtra(EXTRA_DEADLINE, task.deadline);
        intent.putExtra(EXTRA_PRIORITY, task.priority);
        intent.putExtra(EXTRA_STATUS, task.status);
        context.startActivity(intent);
    }

    private TeamTaskFormViewModel viewModel;

    private TextInputEditText editTitle;
    private TextInputEditText editDescription;
    private TextView textSelectedDeadline;
    private MaterialButtonToggleGroup toggleStatus;
    private MaterialSwitch switchPriority;

    private String editingTaskId;        // null = mode tambah
    private long selectedDeadline = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        String teamId = getIntent().getStringExtra(EXTRA_TEAM_ID);
        viewModel = new ViewModelProvider(this, new TeamTaskFormViewModelFactory(teamId))
                .get(TeamTaskFormViewModel.class);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        textSelectedDeadline = findViewById(R.id.textSelectedDeadline);
        toggleStatus = findViewById(R.id.toggleStatus);
        switchPriority = findViewById(R.id.switchPriority);
        View buttonPickDeadline = findViewById(R.id.buttonPickDeadline);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);

        prefillIfEditing();

        viewModel.getSaved().observe(this, saved -> {
            if (Boolean.TRUE.equals(saved)) {
                finish();
            }
        });
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });

        buttonPickDeadline.setOnClickListener(v -> showDateTimePicker());
        buttonSave.setOnClickListener(v -> save());
    }

    private void prefillIfEditing() {
        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_TASK_ID)) {
            setupHeader(R.string.title_add_task);
            toggleStatus.check(statusButtonId(TaskStatus.indexOf(TaskStatus.NOT_STARTED)));
            refreshDeadlineLabel();
            return;
        }
        setupHeader(R.string.title_edit_task);
        editingTaskId = intent.getStringExtra(EXTRA_TASK_ID);
        editTitle.setText(intent.getStringExtra(EXTRA_TITLE));
        editDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
        switchPriority.setChecked(
                intent.getIntExtra(EXTRA_PRIORITY, Task.PRIORITY_NORMAL) == Task.PRIORITY_HIGH);

        int statusIdx = TaskStatus.indexOf(intent.getStringExtra(EXTRA_STATUS));
        toggleStatus.check(statusButtonId(statusIdx >= 0 ? statusIdx : 0));

        selectedDeadline = intent.getLongExtra(EXTRA_DEADLINE, 0L);
        refreshDeadlineLabel();
    }

    private void showDateTimePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDeadline > 0) {
            cal.setTimeInMillis(selectedDeadline);
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.YEAR, year);
            picked.set(Calendar.MONTH, month);
            picked.set(Calendar.DAY_OF_MONTH, day);
            new TimePickerDialog(this, (tp, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                picked.set(Calendar.MILLISECOND, 0);
                selectedDeadline = picked.getTimeInMillis();
                refreshDeadlineLabel();
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void refreshDeadlineLabel() {
        if (selectedDeadline > 0) {
            textSelectedDeadline.setText(DateTimeFormatter.formatDateTime(selectedDeadline));
        } else {
            textSelectedDeadline.setText(R.string.no_deadline);
        }
    }

    private void save() {
        String title = editTitle.getText() == null ? "" : editTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            editTitle.setError(getString(R.string.error_title_required));
            editTitle.requestFocus();
            return;
        }

        TeamTask task = new TeamTask();
        task.id = editingTaskId;          // null → create; terisi → update
        task.title = title;
        task.description = editDescription.getText() == null
                ? "" : editDescription.getText().toString().trim();
        task.deadline = selectedDeadline;
        task.status = TaskStatus.VALUES[statusIndex()];
        task.priority = switchPriority.isChecked() ? Task.PRIORITY_HIGH : Task.PRIORITY_NORMAL;

        viewModel.save(task);
    }

    /** Index status terpilih (paralel {@link TaskStatus#VALUES}). */
    private int statusIndex() {
        int id = toggleStatus.getCheckedButtonId();
        if (id == R.id.btnStatusInProgress) {
            return 1;
        }
        if (id == R.id.btnStatusDone) {
            return 2;
        }
        return 0;
    }

    private int statusButtonId(int index) {
        switch (index) {
            case 1:
                return R.id.btnStatusInProgress;
            case 2:
                return R.id.btnStatusDone;
            default:
                return R.id.btnStatusNotStarted;
        }
    }
}
