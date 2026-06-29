package com.example.taskreminder2.ui.taskdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.example.taskreminder2.ui.TaskViewBinder;
import com.example.taskreminder2.ui.tasklist.TaskFormActivity;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Layar detail tugas: menampilkan info tugas + section "Riwayat Aktivitas"
 * (Fitur-02). Edit/Hapus lewat menu toolbar. Day 10 menambah input catatan
 * manual (Fitur-05) di layar ini.
 */
public class TaskDetailActivity extends BaseToolbarActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    /** Membuka detail untuk sebuah taskId. */
    public static void start(Context context, int taskId) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startActivity(intent);
    }

    private TaskDetailViewModel viewModel;
    private Task currentTask;

    private TextView textTitle;
    private TextView textStatusLabel;
    private TextView textPriority;
    private TextView textStatus;
    private TextView textDeadline;
    private TextView textDescription;
    private TextView textLogsEmpty;
    private TextView textProgressPercent;
    private CircularProgressIndicator progressRing;
    private int defaultDeadlineColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        setupHeader(R.string.title_detail);
        setupHeaderMenu();

        textTitle = findViewById(R.id.textDetailTitle);
        textStatusLabel = findViewById(R.id.textDetailStatusLabel);
        textPriority = findViewById(R.id.textDetailPriority);
        textStatus = findViewById(R.id.textDetailStatus);
        textDeadline = findViewById(R.id.textDetailDeadline);
        textDescription = findViewById(R.id.textDetailDescription);
        textLogsEmpty = findViewById(R.id.textLogsEmpty);
        textProgressPercent = findViewById(R.id.textProgressPercent);
        progressRing = findViewById(R.id.progressRing);
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
        textTitle.setText(task.title);

        TaskViewBinder.bindDetailStatusLabel(textStatusLabel, task.deadline, task.status);

        textPriority.setVisibility(task.priority == Task.PRIORITY_HIGH
                ? TextView.VISIBLE : TextView.GONE);

        textStatus.setText(TaskStatus.label(task.status));

        TaskViewBinder.bindDeadline(textDeadline, task, defaultDeadlineColor);

        int progress = TaskViewBinder.progressForStatus(task.status);
        progressRing.setProgress(progress);
        textProgressPercent.setText(getString(R.string.percent_format, progress));

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

    /** Aktifkan tombol aksi (titik tiga) di header → menu Edit/Hapus. */
    private void setupHeaderMenu() {
        View action = findViewById(R.id.btnHeaderAction);
        ImageView icon = findViewById(R.id.imageHeaderAction);
        icon.setVisibility(View.VISIBLE);
        action.setClickable(true);
        action.setOnClickListener(this::showMenu);
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenuInflater().inflate(R.menu.menu_task_detail, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
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
            return false;
        });
        menu.show();
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
