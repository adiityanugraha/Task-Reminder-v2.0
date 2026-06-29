package com.example.taskreminder2.ui.tasklist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Layar utama (Beranda, desain Emerald Sand): header + week strip + daftar
 * tugas gaya kartu + bottom navigation. Hanya bicara ke {@link TaskListViewModel}.
 *
 * <p>Bottom nav: tab Beranda aktif; Profil membuka Team Mode; Kalender &
 * Checklist placeholder sampai fiturnya dibuat.</p>
 */
public class TaskListActivity extends BaseToolbarActivity
        implements TaskAdapter.OnTaskInteractionListener {

    private static final Locale LOCALE_ID = new Locale("id", "ID");

    private TaskListViewModel viewModel;
    private RecyclerView recycler;
    private TextView textEmpty;
    private boolean filtering;

    /** Hasil permintaan izin notifikasi diabaikan: app tetap jalan tanpa izin. */
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        textEmpty = findViewById(R.id.textEmpty);
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        recycler = findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        TaskAdapter adapter = new TaskAdapter(this);
        recycler.setAdapter(adapter);

        View emptyState = findViewById(R.id.emptyState);
        MaterialButton btnEmptyAdd = findViewById(R.id.btnEmptyAdd);
        btnEmptyAdd.setOnClickListener(v ->
                startActivity(new Intent(this, TaskFormActivity.class)));

        viewModel = new ViewModelProvider(
                this, new TaskListViewModelFactory(getApplication()))
                .get(TaskListViewModel.class);

        viewModel.getTasks().observe(this, tasks -> {
            adapter.submitList(tasks);
            boolean empty = tasks == null || tasks.isEmpty();
            textEmpty.setText(filtering ? R.string.empty_filtered : R.string.empty_tasks);
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            // Tombol tambah tak relevan saat hasil filter kosong.
            btnEmptyAdd.setVisibility(filtering ? View.GONE : View.VISIBLE);
        });

        SearchFilterBinder.bind(editSearch, chipGroup, query -> {
            filtering = query.isFiltering();
            viewModel.setQuery(query);
        });

        setupHeaderDate();
        buildWeekStrip();
        setupNavigation();

        maybeRequestNotificationPermission();
    }

    /** Tanggal hari ini, mis. "Senin, 29 Juni". */
    private void setupHeaderDate() {
        TextView textDate = findViewById(R.id.textDate);
        textDate.setText(new SimpleDateFormat("EEEE, d MMMM", LOCALE_ID)
                .format(Calendar.getInstance().getTime()));
    }

    /** Isi week-strip: 7 hari minggu berjalan (Senin–Minggu), hari ini disorot. */
    private void buildWeekStrip() {
        LinearLayout weekStrip = findViewById(R.id.weekStrip);
        weekStrip.removeAllViews();

        Calendar today = Calendar.getInstance();
        Calendar cursor = (Calendar) today.clone();
        cursor.setFirstDayOfWeek(Calendar.MONDAY);
        // Mundur ke hari Senin minggu ini.
        int dow = cursor.get(Calendar.DAY_OF_WEEK);
        int backToMonday = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
        cursor.add(Calendar.DAY_OF_MONTH, backToMonday);

        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", LOCALE_ID);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < 7; i++) {
            View pill = inflater.inflate(R.layout.item_week_day, weekStrip, false);
            TextView textDay = pill.findViewById(R.id.textDay);
            TextView textNum = pill.findViewById(R.id.textNum);
            textDay.setText(dayFmt.format(cursor.getTime()));
            textNum.setText(String.valueOf(cursor.get(Calendar.DAY_OF_MONTH)));

            if (isSameDay(cursor, today)) {
                pill.setBackgroundResource(R.drawable.bg_day_today);
                textDay.setTextColor(0xCCFFFFFF);
                textNum.setTextColor(0xFFFFFFFF);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            params.setMarginStart(dp(3));
            params.setMarginEnd(dp(3));
            weekStrip.addView(pill, params);

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void setupNavigation() {
        // Tombol tambah di header.
        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(this, TaskFormActivity.class)));

        // Bottom nav.
        findViewById(R.id.navHome).setOnClickListener(v -> recycler.smoothScrollToPosition(0));
        findViewById(R.id.navPerson).setOnClickListener(v ->
                // LoginActivity adalah gerbang: kalau sudah ada sesi, lanjut ke TeamHome.
                startActivity(new Intent(this, LoginActivity.class)));
        View.OnClickListener comingSoon = v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
        findViewById(R.id.navCalendar).setOnClickListener(comingSoon);
        findViewById(R.id.navChecklist).setOnClickListener(comingSoon);
    }

    /** API 33+ butuh izin runtime POST_NOTIFICATIONS agar pengingat bisa tampil. */
    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onTaskClick(Task task) {
        // Klik membuka layar detail (info + riwayat). Edit diakses dari sana.
        TaskDetailActivity.start(this, task.id);
    }

    @Override
    public void onTaskLongClick(@NonNull Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> viewModel.delete(task))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
