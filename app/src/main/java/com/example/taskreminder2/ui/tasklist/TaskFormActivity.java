package com.example.taskreminder2.ui.tasklist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.notification.AlarmReminderScheduler;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.example.taskreminder2.ui.StatusToggle;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * Form tambah/edit tugas (Fitur-01, desain Emerald Sand). Mode ditentukan dari
 * ada-tidaknya {@link #EXTRA_ID} pada Intent: ada → edit (prefill), tidak ada → tambah.
 *
 * <p>Menulis lewat {@link TaskListViewModel} (insert/update) — Activity tidak
 * pernah memanggil Repository langsung. Status memakai segmented control
 * (toggle group) yang index-nya paralel dengan {@link TaskStatus#VALUES}.</p>
 */
public class TaskFormActivity extends BaseToolbarActivity {

    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DESCRIPTION = "extra_description";
    private static final String EXTRA_DEADLINE = "extra_deadline";
    private static final String EXTRA_COURSE_ID = "extra_course_id";
    private static final String EXTRA_STATUS = "extra_status";
    private static final String EXTRA_PRIORITY = "extra_priority";

    /** Mengisi Intent dengan data sebuah Task untuk dibuka dalam mode edit. */
    public static void putTaskExtras(Intent intent, Task task) {
        intent.putExtra(EXTRA_ID, task.id);
        intent.putExtra(EXTRA_TITLE, task.title);
        intent.putExtra(EXTRA_DESCRIPTION, task.description);
        intent.putExtra(EXTRA_DEADLINE, task.deadline);
        intent.putExtra(EXTRA_COURSE_ID, task.courseId);
        intent.putExtra(EXTRA_STATUS, task.status);
        intent.putExtra(EXTRA_PRIORITY, task.priority);
    }

    private TaskListViewModel viewModel;

    private TextInputEditText editTitle;
    private TextInputEditText editDescription;
    private TextView textSelectedDeadline;
    private MaterialButtonToggleGroup toggleStatus;
    private MaterialSwitch switchPriority;

    private int editingId = 0;          // 0 = mode tambah
    private int courseId = 0;
    private long selectedDeadline = 0L; // 0 = belum dipilih

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        textSelectedDeadline = findViewById(R.id.textSelectedDeadline);
        toggleStatus = findViewById(R.id.toggleStatus);
        switchPriority = findViewById(R.id.switchPriority);
        View buttonPickDeadline = findViewById(R.id.buttonPickDeadline);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);

        viewModel = new ViewModelProvider(
                this, new TaskListViewModelFactory(getApplication()))
                .get(TaskListViewModel.class);

        prefillIfEditing();

        buttonPickDeadline.setOnClickListener(v -> showDateTimePicker());
        buttonSave.setOnClickListener(v -> save());
    }

    private void prefillIfEditing() {
        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_ID)) {
            setupHeader(R.string.title_add_task);
            toggleStatus.check(StatusToggle.buttonId(TaskStatus.indexOf(TaskStatus.NOT_STARTED)));
            refreshDeadlineLabel();
            return;
        }
        setupHeader(R.string.title_edit_task);
        editingId = intent.getIntExtra(EXTRA_ID, 0);
        courseId = intent.getIntExtra(EXTRA_COURSE_ID, 0);
        editTitle.setText(intent.getStringExtra(EXTRA_TITLE));
        editDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
        switchPriority.setChecked(
                intent.getIntExtra(EXTRA_PRIORITY, Task.PRIORITY_NORMAL) == Task.PRIORITY_HIGH);

        int statusIdx = TaskStatus.indexOf(intent.getStringExtra(EXTRA_STATUS));
        toggleStatus.check(StatusToggle.buttonId(statusIdx >= 0 ? statusIdx : 0));

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

        Task task = new Task();
        task.id = editingId;            // 0 → Room autoGenerate; >0 → update baris ini
        task.title = title;
        task.description = editDescription.getText() == null
                ? "" : editDescription.getText().toString().trim();
        task.deadline = selectedDeadline;
        task.courseId = courseId;
        task.status = TaskStatus.VALUES[StatusToggle.indexOf(toggleStatus)];
        task.priority = switchPriority.isChecked() ? Task.PRIORITY_HIGH : Task.PRIORITY_NORMAL;
        task.updatedAt = System.currentTimeMillis();

        if (editingId > 0) {
            viewModel.update(task);
        } else {
            viewModel.insert(task);
        }

        // Tugas berdeadline butuh exact alarm agar pengingat tepat waktu (Android 12+).
        if (needsExactAlarmConsent(task)) {
            promptExactAlarm();
        } else {
            finish();
        }
    }

    private boolean needsExactAlarmConsent(Task task) {
        return task.deadline > System.currentTimeMillis()
                && !TaskStatus.DONE.equals(task.status)
                && !AlarmReminderScheduler.canScheduleExact(this);
    }

    private void promptExactAlarm() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.exact_alarm_dialog_title)
                .setMessage(R.string.exact_alarm_dialog_message)
                .setPositiveButton(R.string.action_open_settings, (d, w) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                    }
                    finish();
                })
                .setNegativeButton(R.string.action_later, (d, w) -> finish())
                .setOnCancelListener(d -> finish())
                .show();
    }
}
