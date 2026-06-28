package com.example.taskreminder2.data.model;

import com.google.firebase.firestore.Exclude;

/**
 * Tugas Team Mode (POJO Firestore: teams/{teamId}/tasks/{taskId}).
 *
 * <p>Bukan Room Entity — Team Mode pakai Firestore + offline cache bawaannya
 * (blueprint Bagian 3.3). Field publik agar bisa di-(de)serialize Firestore.
 * {@code id} di-{@link Exclude} dari dokumen karena itu adalah document ID
 * (diisi manual dari {@code DocumentSnapshot.getId()} saat membaca).</p>
 *
 * <p>{@code deadline} & {@code updatedAt} memakai epoch millis (long) — sama
 * seperti Personal Mode, agar {@code OverdueChecker} bisa dipakai ulang.</p>
 */
public class TeamTask {

    @Exclude
    public String id;

    public String title;
    public String description;
    public long deadline;          // epoch millis (0 = tanpa deadline)
    public int priority;           // 0 / 1 (Fitur-06)
    public String status;
    public String assignedTo;
    public String createdBy;
    public long updatedAt;         // epoch millis
    public String updatedBy;       // dipakai membedakan perubahan sendiri vs orang lain

    public TeamTask() {
    }
}
