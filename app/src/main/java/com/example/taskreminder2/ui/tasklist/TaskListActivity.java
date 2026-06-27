package com.example.taskreminder2.ui.tasklist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.ui.taskdetail.TaskDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.TextView;

/**
 * Layar utama (launcher): daftar tugas Personal Mode.
 *
 * <p>Hanya bicara ke {@link TaskListViewModel} — mengamati {@code LiveData},
 * tidak pernah menyentuh Repository/DAO langsung. Fitur-01.</p>
 */
public class TaskListActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskInteractionListener {

    private TaskListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recycler = findViewById(R.id.recyclerTasks);
        TextView textEmpty = findViewById(R.id.textEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        TaskAdapter adapter = new TaskAdapter(this);
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(
                this, new TaskListViewModelFactory(getApplication()))
                .get(TaskListViewModel.class);

        viewModel.getAllTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            textEmpty.setVisibility(tasks == null || tasks.isEmpty() ? TextView.VISIBLE : TextView.GONE);
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, TaskFormActivity.class)));
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
