package com.example.taskreminder2.data.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskreminder2.data.local.dao.TaskDao;
import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.notification.ReminderScheduler;
import com.example.taskreminder2.util.TaskStatus;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

/**
 * Unit test {@link TaskRepository}: memverifikasi auto-logging (Fitur-02) dan
 * penyusunan pesan diff {@code buildUpdateLog}. DAO & log-repo di-mock, executor
 * dibuat sinkron agar tidak perlu emulator/thread.
 */
public class TaskRepositoryTest {

    private TaskDao dao;
    private TaskLogRepository logRepo;
    private ReminderScheduler scheduler;
    private TaskRepository repo;

    @Before
    public void setup() {
        dao = mock(TaskDao.class);
        logRepo = mock(TaskLogRepository.class);
        scheduler = mock(ReminderScheduler.class);
        ExecutorService directExecutor = mock(ExecutorService.class);
        // Jalankan Runnable langsung di thread test (sinkron).
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(directExecutor).execute(any(Runnable.class));
        repo = new TaskRepository(dao, logRepo, directExecutor, scheduler);
    }

    @Test
    public void insert_logsTugasDibuat_withGeneratedId() {
        Task t = task("A", "", 0, 0, TaskStatus.NOT_STARTED);
        when(dao.insert(t)).thenReturn(7L);

        repo.insert(t);

        verify(dao).insert(t);
        verify(logRepo).logActivity(7, "Tugas dibuat");
    }

    @Test
    public void insert_setsGeneratedId_andSchedulesReminder() {
        Task t = task("A", "", 0, 0, TaskStatus.NOT_STARTED);
        when(dao.insert(t)).thenReturn(7L);

        repo.insert(t);

        // Id hasil generate dipasang ke objek sebelum dijadwalkan (penting agar
        // PendingIntent memakai requestCode = id yang benar).
        assertEquals(7, t.id);
        verify(scheduler).schedule(t);
    }

    @Test
    public void update_logsDiff_andCallsDao() {
        Task before = task("A", "", 0, 0, TaskStatus.NOT_STARTED);
        before.id = 3;
        Task after = task("A", "", 0, 1, TaskStatus.NOT_STARTED);
        after.id = 3;
        when(dao.getByIdSync(3)).thenReturn(before);

        repo.update(after);

        verify(dao).update(after);
        verify(logRepo).logActivity(3, "Prioritas diubah menjadi Tinggi");
        verify(scheduler).schedule(after);
    }

    @Test
    public void delete_cancelsReminder() {
        Task t = task("A", "", 0, 0, TaskStatus.NOT_STARTED);
        t.id = 5;

        repo.delete(t);

        verify(dao).delete(t);
        verify(scheduler).cancel(5);
    }

    // --- buildUpdateLog (static, murni) ---

    @Test
    public void buildUpdateLog_nullBefore() {
        assertEquals("Tugas diperbarui", TaskRepository.buildUpdateLog(null, new Task()));
    }

    @Test
    public void buildUpdateLog_noChange() {
        Task a = task("X", "d", 100, 0, TaskStatus.NOT_STARTED);
        Task b = task("X", "d", 100, 0, TaskStatus.NOT_STARTED);
        assertEquals("Tugas diperbarui", TaskRepository.buildUpdateLog(a, b));
    }

    @Test
    public void buildUpdateLog_statusChange() {
        Task a = task("X", "", 0, 0, TaskStatus.NOT_STARTED);
        Task b = task("X", "", 0, 0, TaskStatus.DONE);
        assertEquals("Status diubah menjadi Selesai", TaskRepository.buildUpdateLog(a, b));
    }

    @Test
    public void buildUpdateLog_multipleChanges_joinedAndCapitalized() {
        Task a = task("X", "", 0, 0, TaskStatus.NOT_STARTED);
        Task b = task("Y", "", 0, 1, TaskStatus.NOT_STARTED);
        assertEquals("Judul diubah, prioritas diubah menjadi Tinggi",
                TaskRepository.buildUpdateLog(a, b));
    }

    @Test
    public void buildUpdateLog_deadlineChange() {
        Task a = task("X", "", 0, 0, TaskStatus.NOT_STARTED);
        Task b = task("X", "", 999, 0, TaskStatus.NOT_STARTED);
        assertEquals("Deadline diubah", TaskRepository.buildUpdateLog(a, b));
    }

    private static Task task(String title, String desc, long deadline, int priority, String status) {
        Task t = new Task();
        t.title = title;
        t.description = desc;
        t.deadline = deadline;
        t.priority = priority;
        t.status = status;
        return t;
    }
}
