package com.example.taskreminder2.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Tugas (Personal Mode). Tabel {@code tasks}.
 *
 * <p>Catatan desain: TIDAK ada kolom {@code isOverdue}. Status terlambat
 * (Fitur-08) dihitung saat dibutuhkan dari {@code deadline}, {@code status},
 * dan waktu sekarang — tidak disimpan, agar tidak basi & tidak memicu loop
 * penulisan saat membaca (lihat blueprint Fitur-08).</p>
 */
@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;

    /** Deadline dalam epoch millis. */
    public long deadline;

    /** Referensi ke {@link Course#id} (tanpa foreign key, sesuai blueprint). */
    public int courseId;

    public String status;

    /** 0 = normal, 1 = tinggi (Fitur-06). */
    public int priority;

    /** Epoch millis perubahan terakhir; dipakai urutan log (Fitur-02). */
    public long updatedAt;
}
