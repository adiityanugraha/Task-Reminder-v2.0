package com.example.taskreminder2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskreminder2.data.local.entity.Course;

import java.util.List;

/** DAO untuk Entity {@link Course}. */
@Dao
public interface CourseDao {

    /** @return id baris yang baru dimasukkan. */
    @Insert
    long insert(Course course);

    @Update
    void update(Course course);

    @Delete
    void delete(Course course);

    @Query("SELECT * FROM courses ORDER BY name ASC")
    LiveData<List<Course>> getAllCourses();
}
