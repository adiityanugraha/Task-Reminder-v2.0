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
import com.example.taskreminder2.data.model.Member;
import com.google.android.material.button.MaterialButton;

/**
 * Adapter daftar anggota. Tombol "Keluarkan" hanya tampil bila viewer adalah
 * owner DAN baris itu member (bukan owner, bukan diri sendiri) — Day 17.
 */
public class MemberAdapter extends ListAdapter<Member, MemberAdapter.MemberViewHolder> {

    public interface OnRemoveListener {
        void onRemove(Member member);
    }

    private final boolean viewerIsOwner;
    private final String currentUid;
    private final OnRemoveListener removeListener;

    public MemberAdapter(boolean viewerIsOwner, String currentUid, OnRemoveListener removeListener) {
        super(DIFF_CALLBACK);
        this.viewerIsOwner = viewerIsOwner;
        this.currentUid = currentUid;
        this.removeListener = removeListener;
    }

    private static final DiffUtil.ItemCallback<Member> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Member>() {
                @Override
                public boolean areItemsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
                    return oldItem.uid != null && oldItem.uid.equals(newItem.uid);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
                    return eq(oldItem.displayName, newItem.displayName) && eq(oldItem.role, newItem.role);
                }

                private boolean eq(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textRole;
        private final MaterialButton buttonRemove;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textMemberName);
            textRole = itemView.findViewById(R.id.textMemberRole);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveMember);
        }

        void bind(Member member) {
            textName.setText(member.displayName);
            textRole.setText(member.isOwner()
                    ? R.string.role_owner : R.string.role_member);

            boolean canRemove = viewerIsOwner
                    && !member.isOwner()
                    && (currentUid == null || !currentUid.equals(member.uid));
            buttonRemove.setVisibility(canRemove ? View.VISIBLE : View.GONE);
            buttonRemove.setOnClickListener(canRemove ? v -> removeListener.onRemove(member) : null);
        }
    }
}
