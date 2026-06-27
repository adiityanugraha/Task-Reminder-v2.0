package com.example.taskreminder2.ui;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.OverdueChecker;

/**
 * Helper UI bersama untuk menampilkan field tugas secara konsisten di beberapa
 * layar (list & detail), menghindari duplikasi logika tampilan.
 */
public final class TaskViewBinder {

    /**
     * Mengisi TextView deadline: format tanggal; jika terlambat (Fitur-08) beri
     * warna merah + label "Terlambat". {@code defaultColor} dipulihkan saat
     * tidak terlambat / tanpa deadline — penting karena ViewHolder didaur ulang.
     */
    public static void bindDeadline(TextView view, Task task, int defaultColor) {
        Context ctx = view.getContext();
        if (task.deadline <= 0) {
            view.setText(R.string.list_no_deadline);
            view.setTextColor(defaultColor);
            return;
        }
        String formatted = DateTimeFormatter.formatDateTime(task.deadline);
        if (OverdueChecker.isOverdue(task.deadline, task.status)) {
            view.setText(formatted + "  •  " + ctx.getString(R.string.label_overdue));
            view.setTextColor(ContextCompat.getColor(ctx, R.color.overdue));
        } else {
            view.setText(formatted);
            view.setTextColor(defaultColor);
        }
    }

    private TaskViewBinder() {
    }
}
