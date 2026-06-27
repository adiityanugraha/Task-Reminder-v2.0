package com.example.taskreminder2.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.taskreminder2.data.local.dao.CourseDao;
import com.example.taskreminder2.data.local.dao.TaskDao;
import com.example.taskreminder2.data.local.dao.TaskLogDao;
import com.example.taskreminder2.data.local.entity.Course;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.local.entity.TaskLog;

/**
 * Database Room aplikasi (Personal Mode).
 *
 * <p>version = 1 (project baru, bukan migrasi dari database lama).
 * exportSchema = true menulis skema JSON ke {@code app/schemas} (lokasi
 * diset lewat {@code room.schemaLocation} di build.gradle) untuk validasi
 * Migration di masa depan.</p>
 *
 * <p>version 2 (Day 8): menambah Entity {@code TaskLog}. Selama pra-rilis
 * perubahan skema memakai {@code fallbackToDestructiveMigration} — data lama
 * di-reset saat version naik (aman karena belum ada user/data nyata).</p>
 */
@Database(entities = {Task.class, Course.class, TaskLog.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();

    public abstract CourseDao courseDao();

    public abstract TaskLogDao taskLogDao();

    private static final String DB_NAME = "task_reminder.db";

    private static volatile AppDatabase INSTANCE;

    /** Singleton — satu instance database untuk seluruh app. */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            // Pra-rilis: skema masih berkembang (TaskLog ditambah
                            // Day 8). Belum ada user/data nyata, jadi migrasi
                            // destruktif aman. Diganti Migration sebelum rilis.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
