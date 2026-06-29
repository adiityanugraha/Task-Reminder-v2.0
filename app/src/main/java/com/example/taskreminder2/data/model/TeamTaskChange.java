package com.example.taskreminder2.data.model;

/**
 * Satu perubahan tugas oleh anggota LAIN, hasil deteksi snapshot listener
 * (Fitur-07 Team Mode, Day 27). POJO murni tanpa dependency Android/Firestore
 * agar logika deteksi di Repository tetap mudah dipahami & ditest; pembentukan
 * teks notifikasi (butuh resource string) dilakukan di layer UI.
 */
public class TeamTaskChange {

    public enum Type { ADDED, MODIFIED, REMOVED }

    public final String title;
    public final Type type;

    public TeamTaskChange(String title, Type type) {
        this.title = title;
        this.type = type;
    }
}
