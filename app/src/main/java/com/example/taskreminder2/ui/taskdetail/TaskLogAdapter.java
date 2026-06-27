package com.example.taskreminder2.ui.taskdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.TaskLog;
import com.example.taskreminder2.util.LogType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter daftar riwayat (Fitur-02 ACTIVITY & Fitur-05 NOTE), urut terbaru
 * di atas. Tipe log ditampilkan sebagai label agar aktivitas otomatis dan
 * catatan manual mudah dibedakan.
 */
public class TaskLogAdapter extends ListAdapter<TaskLog, TaskLogAdapter.LogViewHolder> {

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    public TaskLogAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TaskLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TaskLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull TaskLog oldItem, @NonNull TaskLog newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull TaskLog oldItem, @NonNull TaskLog newItem) {
                    return oldItem.createdAt == newItem.createdAt
                            && eq(oldItem.content, newItem.content)
                            && eq(oldItem.logType, newItem.logType);
                }

                private boolean eq(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView textLogType;
        private final TextView textLogContent;
        private final TextView textLogTime;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textLogType = itemView.findViewById(R.id.textLogType);
            textLogContent = itemView.findViewById(R.id.textLogContent);
            textLogTime = itemView.findViewById(R.id.textLogTime);
        }

        void bind(TaskLog log) {
            int typeRes = LogType.NOTE.equals(log.logType)
                    ? R.string.log_type_note : R.string.log_type_activity;
            textLogType.setText(typeRes);
            textLogContent.setText(log.content);
            textLogTime.setText(TIME_FORMAT.format(new Date(log.createdAt)));
        }
    }
}
