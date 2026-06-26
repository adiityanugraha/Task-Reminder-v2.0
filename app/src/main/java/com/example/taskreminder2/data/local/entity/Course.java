package com.example.taskreminder2.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mata kuliah / kategori tugas (Personal Mode). Tabel {@code courses}.
 *
 * <p>Setara tabel {@code courses} versi sebelumnya — strukturnya tidak
 * berubah secara konsep, hanya dipindah jadi {@code @Entity} Room.</p>
 */
@Entity(tableName = "courses")
public class Course {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
}
