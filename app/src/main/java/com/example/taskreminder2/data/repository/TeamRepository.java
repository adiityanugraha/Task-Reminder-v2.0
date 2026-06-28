package com.example.taskreminder2.data.repository;

import androidx.annotation.Nullable;

import com.example.taskreminder2.data.model.Team;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logika team Mode (Fitur-04): buat team, join via kode, dan daftar team
 * milik user. Menulis ke Firestore lewat {@link WriteBatch} (atomic).
 *
 * <p>Struktur (blueprint Bagian 3.2): {@code teams/{teamId}},
 * {@code teams/{teamId}/members/{uid}}, dan {@code users/{uid}.teams[]}.</p>
 */
public class TeamRepository {

    /** Callback hasil operasi. {@code info} = inviteCode (create) / nama team (join). */
    public interface TeamCallback {
        void onResult(boolean success, @Nullable String info, @Nullable String errorMessage);
    }

    public interface TeamsCallback {
        void onResult(List<Team> teams);
    }

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public TeamRepository() {
        this(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    public TeamRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
    }

    public void createTeam(String teamName, TeamCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(false, null, "Belum login");
            return;
        }
        String uid = user.getUid();
        String inviteCode = generateInviteCode();
        DocumentReference teamRef = db.collection("teams").document();
        String teamId = teamRef.getId();

        Map<String, Object> team = new HashMap<>();
        team.put("name", teamName);
        team.put("ownerId", uid);
        team.put("inviteCode", inviteCode);
        team.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(teamRef, team);
        batch.set(teamRef.collection("members").document(uid), memberDoc(user, "owner"));
        batch.set(db.collection("users").document(uid), userDoc(user, teamId), SetOptions.merge());

        batch.commit()
                .addOnSuccessListener(x -> callback.onResult(true, inviteCode, null))
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    public void joinTeamByCode(String code, TeamCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(false, null, "Belum login");
            return;
        }
        String uid = user.getUid();

        db.collection("teams")
                .whereEqualTo("inviteCode", code)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        callback.onResult(false, null, "Kode team tidak ditemukan");
                        return;
                    }
                    DocumentSnapshot teamDoc = snap.getDocuments().get(0);
                    String teamId = teamDoc.getId();
                    String teamName = teamDoc.getString("name");

                    WriteBatch batch = db.batch();
                    batch.set(teamDoc.getReference().collection("members").document(uid),
                            memberDoc(user, "member"));
                    batch.set(db.collection("users").document(uid),
                            userDoc(user, teamId), SetOptions.merge());

                    batch.commit()
                            .addOnSuccessListener(x -> callback.onResult(true, teamName, null))
                            .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onResult(false, null, e.getMessage()));
    }

    /** Memuat daftar team yang diikuti user (dari {@code users/{uid}.teams}). */
    public void loadUserTeams(TeamsCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onResult(new ArrayList<>());
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    @SuppressWarnings("unchecked")
                    List<String> teamIds = (List<String>) doc.get("teams");
                    if (teamIds == null || teamIds.isEmpty()) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }
                    // whereIn documentId dibatasi 10; cukup untuk skala ini.
                    List<String> ids = teamIds.size() > 10 ? teamIds.subList(0, 10) : teamIds;
                    db.collection("teams")
                            .whereIn(FieldPath.documentId(), ids)
                            .get()
                            .addOnSuccessListener(snap -> {
                                List<Team> teams = new ArrayList<>();
                                for (DocumentSnapshot d : snap.getDocuments()) {
                                    teams.add(new Team(d.getId(),
                                            d.getString("name"),
                                            d.getString("inviteCode"),
                                            d.getString("ownerId")));
                                }
                                callback.onResult(teams);
                            })
                            .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    private Map<String, Object> memberDoc(FirebaseUser user, String role) {
        Map<String, Object> member = new HashMap<>();
        member.put("role", role);
        member.put("joinedAt", FieldValue.serverTimestamp());
        member.put("displayName", displayName(user));
        return member;
    }

    private Map<String, Object> userDoc(FirebaseUser user, String teamId) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("displayName", displayName(user));
        userDoc.put("email", user.getEmail());
        userDoc.put("teams", FieldValue.arrayUnion(teamId));
        return userDoc;
    }

    private static String displayName(FirebaseUser user) {
        String name = user.getDisplayName();
        return (name != null && !name.isEmpty()) ? name : user.getEmail();
    }

    private static String generateInviteCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
