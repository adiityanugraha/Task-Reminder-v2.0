package com.example.taskreminder2.data.model;

import com.google.firebase.firestore.Exclude;

/**
 * Satu baris riwayat tugas Team Mode (Firestore:
 * teams/{teamId}/tasks/{taskId}/logs/{logId}). {@code createdBy} menyimpan
 * NAMA anggota agar riwayat bisa menyebut siapa (Fitur-02/05).
 */
public class TeamTaskLog {

    @Exclude
    public String id;

    public String logType;   // "ACTIVITY" | "NOTE"
    public String content;
    public long createdAt;
    public String createdBy;  // displayName anggota

    public TeamTaskLog() {
    }
}
