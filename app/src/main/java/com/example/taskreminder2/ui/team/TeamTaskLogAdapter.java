package com.example.taskreminder2.ui.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.model.TeamTaskLog;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.LogType;

/**
 * Adapter riwayat tugas Team Mode (Fitur-02/05). Menampilkan tipe, isi, dan
 * "waktu • nama anggota" — riwayat menyebut siapa yang melakukan.
 */
public class TeamTaskLogAdapter extends ListAdapter<TeamTaskLog, TeamTaskLogAdapter.LogViewHolder> {

    public TeamTaskLogAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TeamTaskLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TeamTaskLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull TeamTaskLog oldItem, @NonNull TeamTaskLog newItem) {
                    return oldItem.id != null && oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TeamTaskLog oldItem, @NonNull TeamTaskLog newItem) {
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

        void bind(TeamTaskLog log) {
            int typeRes = LogType.NOTE.equals(log.logType)
                    ? R.string.log_type_note : R.string.log_type_activity;
            textLogType.setText(typeRes);
            textLogContent.setText(log.content);
            String time = DateTimeFormatter.formatDateTime(log.createdAt);
            textLogTime.setText(log.createdBy != null ? time + "  •  " + log.createdBy : time);
        }
    }
}
