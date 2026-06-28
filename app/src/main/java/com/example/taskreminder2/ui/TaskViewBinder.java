package com.example.taskreminder2.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.OverdueChecker;
import com.google.android.material.card.MaterialCardView;

/**
 * Helper UI bersama untuk menampilkan field tugas secara konsisten di Personal
 * Mode ({@link Task}) maupun Team Mode (TeamTask). Beroperasi pada field
 * primitif (deadline/status/priority) agar dipakai ulang oleh kedua tipe.
 */
public final class TaskViewBinder {

    /**
     * Mengisi TextView deadline: format tanggal; jika terlambat (Fitur-08) beri
     * warna merah + label "Terlambat". {@code defaultColor} dipulihkan saat
     * tidak terlambat / tanpa deadline — penting karena ViewHolder didaur ulang.
     */
    public static void bindDeadline(TextView view, long deadline, String status, int defaultColor) {
        Context ctx = view.getContext();
        if (deadline <= 0) {
            view.setText(R.string.list_no_deadline);
            view.setTextColor(defaultColor);
            return;
        }
        String formatted = DateTimeFormatter.formatDateTime(deadline);
        if (OverdueChecker.isOverdue(deadline, status)) {
            view.setText(formatted + "  •  " + ctx.getString(R.string.label_overdue));
            view.setTextColor(ContextCompat.getColor(ctx, R.color.overdue));
        } else {
            view.setText(formatted);
            view.setTextColor(defaultColor);
        }
    }

    /** Varian untuk Entity {@link Task} (Personal Mode). */
    public static void bindDeadline(TextView view, Task task, int defaultColor) {
        bindDeadline(view, task.deadline, task.status, defaultColor);
    }

    /**
     * Penanda visual prioritas tinggi (Fitur-06): badge + stroke kartu merah
     * saat {@code priority == }{@link Task#PRIORITY_HIGH}. Di-reset saat normal
     * (penting karena ViewHolder didaur ulang).
     */
    public static void bindPriority(MaterialCardView card, TextView badge, int priority) {
        if (priority == Task.PRIORITY_HIGH) {
            badge.setVisibility(View.VISIBLE);
            Context ctx = card.getContext();
            int color = ContextCompat.getColor(ctx, R.color.priority_high);
            float density = ctx.getResources().getDisplayMetrics().density;
            card.setStrokeColor(color);
            card.setStrokeWidth((int) (1.5f * density));
        } else {
            badge.setVisibility(View.GONE);
            card.setStrokeWidth(0);
        }
    }

    private TaskViewBinder() {
    }
}
