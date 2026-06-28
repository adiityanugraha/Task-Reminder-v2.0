package com.example.taskreminder2.ui.team;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskreminder2.R;
import com.example.taskreminder2.data.model.Member;
import com.example.taskreminder2.data.model.Team;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Kelola Team (Fitur-04, Day 17). Owner bisa mengeluarkan member & menghapus
 * team; member hanya melihat daftar anggota (aksi owner disembunyikan).
 */
public class ManageTeamActivity extends AppCompatActivity implements MemberAdapter.OnRemoveListener {

    private static final String EXTRA_TEAM_ID = "extra_team_id";
    private static final String EXTRA_TEAM_NAME = "extra_team_name";
    private static final String EXTRA_TEAM_CODE = "extra_team_code";
    private static final String EXTRA_OWNER_ID = "extra_owner_id";

    public static void start(Context context, Team team) {
        Intent intent = new Intent(context, ManageTeamActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, team.id);
        intent.putExtra(EXTRA_TEAM_NAME, team.name);
        intent.putExtra(EXTRA_TEAM_CODE, team.inviteCode);
        intent.putExtra(EXTRA_OWNER_ID, team.ownerId);
        context.startActivity(intent);
    }

    private ManageTeamViewModel viewModel;
    private String teamId;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_team);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_manage_team);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(ManageTeamViewModel.class);

        Intent intent = getIntent();
        teamId = intent.getStringExtra(EXTRA_TEAM_ID);
        String teamName = intent.getStringExtra(EXTRA_TEAM_NAME);
        String teamCode = intent.getStringExtra(EXTRA_TEAM_CODE);
        String ownerId = intent.getStringExtra(EXTRA_OWNER_ID);
        isOwner = viewModel.isOwner(ownerId);

        ((TextView) findViewById(R.id.textTeamName)).setText(teamName);
        ((TextView) findViewById(R.id.textTeamCode))
                .setText(getString(R.string.team_code_label, teamCode));

        MaterialButton buttonDelete = findViewById(R.id.buttonDeleteTeam);
        TextView memberNote = findViewById(R.id.textMemberOnlyNote);
        // Aksi owner hanya untuk owner; member melihat catatan read-only.
        buttonDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        memberNote.setVisibility(isOwner ? View.GONE : View.VISIBLE);

        RecyclerView recycler = findViewById(R.id.recyclerMembers);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        MemberAdapter adapter = new MemberAdapter(isOwner, viewModel.currentUid(), this);
        recycler.setAdapter(adapter);

        viewModel.getMembers().observe(this, adapter::submitList);
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getTeamDeleted().observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Toast.makeText(this, R.string.button_delete_team, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        buttonDelete.setOnClickListener(v -> confirmDeleteTeam());

        viewModel.loadMembers(teamId);
    }

    @Override
    public void onRemove(Member member) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_remove_member_title)
                .setMessage(getString(R.string.confirm_remove_member_message, member.displayName))
                .setPositiveButton(R.string.action_remove_member,
                        (d, w) -> viewModel.removeMember(teamId, member))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDeleteTeam() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_team_title)
                .setMessage(R.string.confirm_delete_team_message)
                .setPositiveButton(R.string.button_delete_team, (d, w) -> viewModel.deleteTeam(teamId))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
