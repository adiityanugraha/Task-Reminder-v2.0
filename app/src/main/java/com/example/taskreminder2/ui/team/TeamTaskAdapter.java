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
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.util.DateTimeFormatter;
import com.example.taskreminder2.util.TaskStatus;

/**
 * Adapter daftar tugas Team Mode (Day 19, minimal). Badge prioritas & penanda
 * overdue ditambahkan Day 22 (Fitur-06/08 Team Mode).
 */
public class TeamTaskAdapter extends ListAdapter<TeamTask, TeamTaskAdapter.TaskViewHolder> {

    public interface OnTaskInteractionListener {
        void onTaskLongClick(TeamTask task);
    }

    private final OnTaskInteractionListener listener;

    public TeamTaskAdapter(OnTaskInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TeamTask> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TeamTask>() {
                @Override
                public boolean areItemsTheSame(@NonNull TeamTask oldItem, @NonNull TeamTask newItem) {
                    return oldItem.id != null && oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull TeamTask oldItem, @NonNull TeamTask newItem) {
                    return oldItem.deadline == newItem.deadline
                            && oldItem.priority == newItem.priority
                            && oldItem.updatedAt == newItem.updatedAt
                            && eq(oldItem.title, newItem.title)
                            && eq(oldItem.status, newItem.status);
                }

                private boolean eq(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textDeadline;
        private final TextView textStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDeadline = itemView.findViewById(R.id.textDeadline);
            textStatus = itemView.findViewById(R.id.textStatus);

            itemView.setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onTaskLongClick(getItem(pos));
                    return true;
                }
                return false;
            });
        }

        void bind(TeamTask task) {
            textTitle.setText(task.title);
            textDeadline.setText(task.deadline > 0
                    ? DateTimeFormatter.formatDateTime(task.deadline)
                    : itemView.getContext().getString(R.string.list_no_deadline));
            textStatus.setText(TaskStatus.label(task.status));
        }
    }
}
