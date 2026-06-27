package com.example.taskreminder2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskreminder2.data.local.entity.Task;

import java.util.List;

/**
 * DAO untuk Entity {@link Task}.
 *
 * <p>Day 3: hanya operasi dasar. Query pencarian/filter (Fitur-03) dan
 * query overdue (Fitur-08) ditambahkan pada Day 7 & Day 11.</p>
 */
@Dao
public interface TaskDao {

    /** @return id baris yang baru dimasukkan. */
    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    LiveData<List<Task>> getAllTasks();

    // --- Fitur-03: pencarian & filter ---

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :keyword || '%' ORDER BY deadline ASC")
    LiveData<List<Task>> searchByTitle(String keyword);

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY deadline ASC")
    LiveData<List<Task>> filterByPriority(int priority);

    @Query("SELECT * FROM tasks WHERE courseId = :courseId ORDER BY deadline ASC")
    LiveData<List<Task>> filterByCourse(int courseId);

    // Konsisten dengan OverdueChecker: tugas tanpa deadline (0) tidak terlambat.
    @Query("SELECT * FROM tasks WHERE deadline > 0 AND deadline < :now AND status != 'SELESAI' ORDER BY deadline ASC")
    LiveData<List<Task>> filterOverdue(long now);

    /** Reaktif: dipakai layar detail agar otomatis refresh setelah edit. */
    @Query("SELECT * FROM tasks WHERE id = :id")
    LiveData<Task> getById(int id);

    /** Non-LiveData: dipanggil dari background untuk membandingkan nilai
     *  lama vs baru saat membuat log perubahan (Fitur-02). */
    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getByIdSync(int id);
}
