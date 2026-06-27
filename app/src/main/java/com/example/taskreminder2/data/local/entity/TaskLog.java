package com.example.taskreminder2.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Satu baris riwayat milik sebuah {@link Task}. Tabel {@code task_logs}.
 *
 * <p>Dipakai dua fitur sekaligus, dibedakan {@code logType}
 * ({@link com.example.taskreminder2.util.LogType}):
 * Fitur-02 (riwayat aktivitas otomatis) & Fitur-05 (catatan manual).</p>
 *
 * <p>Foreign key ke {@code Task} dengan {@code ON DELETE CASCADE}: menghapus
 * tugas otomatis menghapus seluruh log-nya. Kolom {@code taskId} di-index
 * agar query per-task cepat dan menghindari peringatan Room soal FK tak
 * ter-index.</p>
 */
@Entity(tableName = "task_logs",
        foreignKeys = @ForeignKey(entity = Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("taskId"))
public class TaskLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int taskId;

    /** "ACTIVITY" (Fitur-02) atau "NOTE" (Fitur-05). */
    public String logType;

    public String content;

    /** Epoch millis saat log dibuat. */
    public long createdAt;
}
