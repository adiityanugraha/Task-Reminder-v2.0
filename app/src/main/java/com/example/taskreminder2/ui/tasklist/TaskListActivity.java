package com.example.taskreminder2.ui.tasklist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.ui.BaseToolbarActivity;
import com.example.taskreminder2.ui.SearchFilterBinder;
import com.example.taskreminder2.ui.taskdetail.TaskDetailActivity;
import com.example.taskreminder2.ui.team.LoginActivity;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Layar utama (launcher): daftar tugas Personal Mode dengan pencarian &
 * filter (Fitur-03). Hanya bicara ke {@link TaskListViewModel}.
 */
public class TaskListActivity extends BaseToolbarActivity
        implements TaskAdapter.OnTaskInteractionListener {

    private TaskListViewModel viewModel;
    private TextView textEmpty;
    private boolean filtering;

    /** Hasil permintaan izin notifikasi diabaikan: app tetap jalan tanpa izin,
     *  notifikasi cukup tidak muncul (lihat NotificationHelper). */
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        setupToolbar(0, false);

        RecyclerView recycler = findViewById(R.id.recyclerTasks);
        textEmpty = findViewById(R.id.textEmpty);
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        TaskAdapter adapter = new TaskAdapter(this);
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(
                this, new TaskListViewModelFactory(getApplication()))
                .get(TaskListViewModel.class);

        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            boolean empty = tasks == null || tasks.isEmpty();
            textEmpty.setText(filtering ? R.string.empty_filtered : R.string.empty_tasks);
            textEmpty.setVisibility(empty ? TextView.VISIBLE : TextView.GONE);
        });

        SearchFilterBinder.bind(editSearch, chipGroup, query -> {
            filtering = query.isFiltering();
            viewModel.setQuery(query);
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, TaskFormActivity.class)));

        maybeRequestNotificationPermission();
    }

    /** API 33+ butuh izin runtime POST_NOTIFICATIONS agar pengingat bisa tampil. */
    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_team_mode) {
            // LoginActivity adalah gerbang: kalau sudah ada sesi, dia langsung
            // meneruskan ke TeamHome.
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskClick(Task task) {
        // Klik membuka layar detail (info + riwayat). Edit diakses dari sana.
        TaskDetailActivity.start(this, task.id);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> viewModel.delete(task))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
