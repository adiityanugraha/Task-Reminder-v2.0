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
import com.example.taskreminder2.data.model.Team;

/** Adapter daftar team milik user. */
public class TeamAdapter extends ListAdapter<Team, TeamAdapter.TeamViewHolder> {

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    private final OnTeamClickListener listener;

    public TeamAdapter(OnTeamClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Team> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Team>() {
                @Override
                public boolean areItemsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return oldItem.id != null && oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return eq(oldItem.name, newItem.name) && eq(oldItem.inviteCode, newItem.inviteCode);
                }

                private boolean eq(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTeamName;
        private final TextView textTeamCode;

        TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            textTeamName = itemView.findViewById(R.id.textTeamName);
            textTeamCode = itemView.findViewById(R.id.textTeamCode);
            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onTeamClick(getItem(pos));
                }
            });
        }

        void bind(Team team) {
            textTeamName.setText(team.name);
            textTeamCode.setText(itemView.getContext().getString(R.string.team_code_label, team.inviteCode));
        }
    }
}
