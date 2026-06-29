package com.example.taskreminder2.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.OverdueChecker;
import com.example.taskreminder2.util.TaskStatus;
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

    /**
     * Mewarnai kotak ikon item tugas (desain Emerald Sand) sesuai keadaan:
     * terlambat → merah, selesai → abu (ikon centang), prioritas → oranye,
     * lainnya → emerald. Di-set eksplisit tiap bind agar aman saat daur ulang.
     */
    public static void bindIconBox(View iconBox, ImageView icon,
                                   long deadline, String status, int priority) {
        Context ctx = iconBox.getContext();
        int bg;
        int tint;
        int iconRes = R.drawable.ic_task;
        if (OverdueChecker.isOverdue(deadline, status)) {
            bg = R.color.es_priority_bg;
            tint = R.color.es_overdue;
        } else if (TaskStatus.DONE.equals(status)) {
            bg = R.color.es_surface_done;
            tint = R.color.es_text_muted;
            iconRes = R.drawable.ic_check;
        } else if (priority == Task.PRIORITY_HIGH) {
            bg = R.color.es_accent_orange_container;
            tint = R.color.es_accent_orange;
        } else {
            bg = R.color.es_primary_container;
            tint = R.color.es_primary;
        }
        iconBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bg)));
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, tint)));
    }

    /**
     * Judul tugas selesai ditampilkan dicoret + redup; selainnya normal.
     * Reset eksplisit penting karena ViewHolder didaur ulang.
     */
    public static void bindTitleState(TextView title, String status) {
        Context ctx = title.getContext();
        if (TaskStatus.DONE.equals(status)) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(ContextCompat.getColor(ctx, R.color.es_text_muted));
        } else {
            title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(ContextCompat.getColor(ctx, R.color.es_text_primary));
        }
    }

    /**
     * Progres ditURUNkan dari status (tidak ada kolom progres): Belum=0,
     * Dikerjakan=50, Selesai=100. Dipakai ring progres di layar detail —
     * placeholder visual sampai fitur progres sungguhan dibuat.
     */
    public static int progressForStatus(String status) {
        if (TaskStatus.DONE.equals(status)) {
            return 100;
        }
        if (TaskStatus.IN_PROGRESS.equals(status)) {
            return 50;
        }
        return 0;
    }

    /**
     * Label keadaan di header detail: TERLAMBAT (merah) / SELESAI (abu) /
     * TUGAS AKTIF (emerald).
     */
    public static void bindDetailStatusLabel(TextView view, long deadline, String status) {
        Context ctx = view.getContext();
        if (OverdueChecker.isOverdue(deadline, status)) {
            view.setText(R.string.detail_label_overdue);
            view.setTextColor(ContextCompat.getColor(ctx, R.color.es_overdue));
        } else if (TaskStatus.DONE.equals(status)) {
            view.setText(R.string.detail_label_done);
            view.setTextColor(ContextCompat.getColor(ctx, R.color.es_text_muted));
        } else {
            view.setText(R.string.detail_label_active);
            view.setTextColor(ContextCompat.getColor(ctx, R.color.es_primary));
        }
    }

    private TaskViewBinder() {
    }
}
