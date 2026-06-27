package com.example.taskreminder2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.taskreminder2.data.local.entity.TaskLog;

import java.util.List;

/** DAO untuk Entity {@link TaskLog}. */
@Dao
public interface TaskLogDao {

    @Insert
    void insert(TaskLog log);

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY createdAt DESC")
    LiveData<List<TaskLog>> getLogsForTask(int taskId);
}
