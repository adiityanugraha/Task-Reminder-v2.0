package com.example.taskreminder2.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.taskreminder2.data.local.dao.CourseDao;
import com.example.taskreminder2.data.local.dao.TaskDao;
import com.example.taskreminder2.data.local.entity.Course;
import com.example.taskreminder2.data.local.entity.Task;

/**
 * Database Room aplikasi (Personal Mode).
 *
 * <p>version = 1 (project baru, bukan migrasi dari database lama).
 * exportSchema = true menulis skema JSON ke {@code app/schemas} (lokasi
 * diset lewat {@code room.schemaLocation} di build.gradle) untuk validasi
 * Migration di masa depan.</p>
 *
 * <p>Entity {@code TaskLog} ditambahkan pada Day 8 (akan menaikkan version).</p>
 */
@Database(entities = {Task.class, Course.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();

    public abstract CourseDao courseDao();
}
