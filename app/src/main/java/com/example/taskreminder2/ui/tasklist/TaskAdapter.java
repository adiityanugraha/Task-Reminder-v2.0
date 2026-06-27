package com.example.taskreminder2.ui.tasklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.util.TaskStatus;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter daftar tugas. Memakai {@link ListAdapter} + {@link DiffUtil}
 * sehingga perubahan dari LiveData hanya me-render ulang item yang benar-benar
 * berubah (bukan {@code notifyDataSetChanged()} menyeluruh).
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    /** Callback interaksi item; di-handle Activity, bukan adapter. */
    public interface OnTaskInteractionListener {
        void onTaskClick(Task task);

        void onTaskLongClick(Task task);
    }

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    private final OnTaskInteractionListener listener;

    public TaskAdapter(OnTaskInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Task>() {
                @Override
                public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
                    return oldItem.deadline == newItem.deadline
                            && oldItem.priority == newItem.priority
                            && oldItem.updatedAt == newItem.updatedAt
                            && equals(oldItem.title, newItem.title)
                            && equals(oldItem.status, newItem.status);
                }

                private boolean equals(String a, String b) {
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

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textTitle = itemView.findViewById(R.id.textTitle);
            textDeadline = itemView.findViewById(R.id.textDeadline);
            textStatus = itemView.findViewById(R.id.textStatus);
            textPriorityBadge = itemView.findViewById(R.id.textPriorityBadge);

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

        void bind(Task task) {
            textTitle.setText(task.title);
            if (task.deadline > 0) {
                textDeadline.setText(DATE_FORMAT.format(new Date(task.deadline)));
            } else {
                textDeadline.setText(R.string.list_no_deadline);
            }
            String[] labels = itemView.getContext().getResources()
                    .getStringArray(R.array.status_labels);
            int statusIdx = TaskStatus.indexOf(task.status);
            textStatus.setText(statusIdx >= 0 ? labels[statusIdx] : task.status);

            // Fitur-06: penanda visual prioritas tinggi (priority == 1).
            if (task.priority == 1) {
                textPriorityBadge.setVisibility(View.VISIBLE);
                int color = ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                float density = itemView.getResources().getDisplayMetrics().density;
                card.setStrokeColor(color);
                card.setStrokeWidth((int) (1.5f * density));
            } else {
                textPriorityBadge.setVisibility(View.GONE);
                card.setStrokeWidth(0);
            }
        }
    }
}
