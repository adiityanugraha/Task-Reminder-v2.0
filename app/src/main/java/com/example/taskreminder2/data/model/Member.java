package com.example.taskreminder2.data.model;

/** Anggota team (Firestore: teams/{teamId}/members/{uid}). */
public class Member {

    public String uid;
    public String displayName;
    public String role; // "owner" | "member"

    public Member() {
    }

    public Member(String uid, String displayName, String role) {
        this.uid = uid;
        this.displayName = displayName;
        this.role = role;
    }

    public boolean isOwner() {
        return "owner".equals(role);
    }
}
