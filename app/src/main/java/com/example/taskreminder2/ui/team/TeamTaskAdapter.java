package com.example.taskreminder2.ui.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.ui.TaskViewBinder;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.card.MaterialCardView;

/**
 * Adapter daftar tugas Team Mode. Memakai layout item & helper tampilan yang
 * sama dengan Personal Mode ({@link TaskViewBinder}) — badge prioritas
 * (Fitur-06) dan penanda overdue (Fitur-08) konsisten di kedua mode.
 */
public class TeamTaskAdapter extends ListAdapter<TeamTask, TeamTaskAdapter.TaskViewHolder> {

    public interface OnTaskInteractionListener {
        void onTaskClick(TeamTask task);

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
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView textTitle;
        private final TextView textDeadline;
        private final TextView textStatus;
        private final TextView textPriorityBadge;
        private final View iconBox;
        private final ImageView imageIcon;
        private final int defaultDeadlineColor;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textTitle = itemView.findViewById(R.id.textTitle);
            textDeadline = itemView.findViewById(R.id.textDeadline);
            textStatus = itemView.findViewById(R.id.textStatus);
            textPriorityBadge = itemView.findViewById(R.id.textPriorityBadge);
            iconBox = itemView.findViewById(R.id.iconBox);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            defaultDeadlineColor = textDeadline.getCurrentTextColor();

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(getItem(pos));
                }
            });
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
            TaskViewBinder.bindTitleState(textTitle, task.status);
            TaskViewBinder.bindDeadline(textDeadline, task.deadline, task.status, defaultDeadlineColor);
            textStatus.setText(TaskStatus.label(task.status));
            TaskViewBinder.bindPriority(card, textPriorityBadge, task.priority);
            TaskViewBinder.bindIconBox(iconBox, imageIcon, task.deadline, task.status, task.priority);
        }
    }
}
