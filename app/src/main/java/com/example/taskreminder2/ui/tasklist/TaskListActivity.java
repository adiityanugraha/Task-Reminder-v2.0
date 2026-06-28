package com.example.taskreminder2.ui.tasklist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.ui.taskdetail.TaskDetailActivity;
import com.example.taskreminder2.ui.team.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Layar utama (launcher): daftar tugas Personal Mode dengan pencarian &
 * filter (Fitur-03). Hanya bicara ke {@link TaskListViewModel}.
 */
public class TaskListActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskInteractionListener {

    private TaskListViewModel viewModel;
    private TextInputEditText editSearch;
    private ChipGroup chipGroup;
    private TextView textEmpty;
    private boolean filtering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recycler = findViewById(R.id.recyclerTasks);
        textEmpty = findViewById(R.id.textEmpty);
        editSearch = findViewById(R.id.editSearch);
        chipGroup = findViewById(R.id.chipGroupFilter);
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

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyQuery();
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyQuery());

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, TaskFormActivity.class)));
    }

    /** Menyusun kriteria dari search box + chip terpilih, kirim ke ViewModel. */
    private void applyQuery() {
        String keyword = editSearch.getText() == null ? "" : editSearch.getText().toString().trim();

        TaskListViewModel.Filter filter;
        int checked = chipGroup.getCheckedChipId();
        if (checked == R.id.chipPriority) {
            filter = TaskListViewModel.Filter.PRIORITY_HIGH;
        } else if (checked == R.id.chipOverdue) {
            filter = TaskListViewModel.Filter.OVERDUE;
        } else {
            filter = TaskListViewModel.Filter.ALL;
        }

        filtering = !keyword.isEmpty() || filter != TaskListViewModel.Filter.ALL;
        viewModel.setQuery(filter, keyword);
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
