package com.example.taskreminder2.ui.taskdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.ui.tasklist.TaskFormActivity;
import com.example.taskreminder2.util.OverdueChecker;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Layar detail tugas: menampilkan info tugas + section "Riwayat Aktivitas"
 * (Fitur-02). Edit/Hapus lewat menu toolbar. Day 10 menambah input catatan
 * manual (Fitur-05) di layar ini.
 */
public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    /** Membuka detail untuk sebuah taskId. */
    public static void start(Context context, int taskId) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startActivity(intent);
    }

    private TaskDetailViewModel viewModel;
    private Task currentTask;

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textTitle = findViewById(R.id.textDetailTitle);
        textPriority = findViewById(R.id.textDetailPriority);
        textStatus = findViewById(R.id.textDetailStatus);
        textDeadline = findViewById(R.id.textDetailDeadline);
        textDescription = findViewById(R.id.textDetailDescription);
        textLogsEmpty = findViewById(R.id.textLogsEmpty);
        defaultDeadlineColor = textDeadline.getCurrentTextColor();

        RecyclerView recyclerLogs = findViewById(R.id.recyclerLogs);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        TaskLogAdapter logAdapter = new TaskLogAdapter();
        recyclerLogs.setAdapter(logAdapter);

        TextInputEditText editNote = findViewById(R.id.editNote);
        MaterialButton buttonAddNote = findViewById(R.id.buttonAddNote);
        buttonAddNote.setOnClickListener(v -> addNote(editNote));

        int taskId = getIntent().getIntExtra(EXTRA_TASK_ID, 0);
        viewModel = new ViewModelProvider(
                this, new TaskDetailViewModelFactory(getApplication(), taskId))
                .get(TaskDetailViewModel.class);

        viewModel.getTask().observe(this, task -> {
            if (task == null) {
                // Tugas dihapus (mis. dari layar ini) — tutup detail.
                finish();
                return;
            }
            currentTask = task;
            bindTask(task);
        });

        viewModel.getLogs().observe(this, logs -> {
            logAdapter.submitList(logs);
            textLogsEmpty.setVisibility(logs == null || logs.isEmpty()
                    ? TextView.VISIBLE : TextView.GONE);
        });
    }

    private void bindTask(Task task) {
        setTitle(task.title);
        textTitle.setText(task.title);

        textPriority.setVisibility(task.priority == 1 ? TextView.VISIBLE : TextView.GONE);

        textStatus.setText(getString(R.string.detail_status, TaskStatus.label(task.status)));

        if (task.deadline > 0) {
            String formatted = DATE_FORMAT.format(new Date(task.deadline));
            if (OverdueChecker.isOverdue(task.deadline, task.status)) {
                textDeadline.setText(formatted + "  •  " + getString(R.string.label_overdue));
                textDeadline.setTextColor(ContextCompat.getColor(this, R.color.overdue));
            } else {
                textDeadline.setText(formatted);
                textDeadline.setTextColor(defaultDeadlineColor);
            }
        } else {
            textDeadline.setText(R.string.list_no_deadline);
            textDeadline.setTextColor(defaultDeadlineColor);
        }

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
        // Sembunyikan keyboard.
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editNote.getWindowToken(), 0);
        }
        Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            if (currentTask != null) {
                Intent intent = new Intent(this, TaskFormActivity.class);
                TaskFormActivity.putTaskExtras(intent, currentTask);
                startActivity(intent);
            }
            return true;
        }
        if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void confirmDelete() {
        if (currentTask == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    viewModel.delete(currentTask);
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
