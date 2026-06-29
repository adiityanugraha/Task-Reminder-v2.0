package com.example.taskreminder2.ui.team;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.model.TeamTask;
import com.example.taskreminder2.data.repository.TeamTaskRepository;
import com.example.taskreminder2.ui.TaskQuery;
import com.example.taskreminder2.util.TaskStatus;
import com.google.firebase.firestore.ListenerRegistration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

/**
 * Unit test {@link TeamTaskViewModel}: memverifikasi filter & pencarian DI MEMORI
 * (Fitur-03) atas {@code List<TeamTask>} dari snapshot listener. Repository
 * di-mock dan callback listener ditangkap lalu dipicu manual — jadi logika
 * filter milik kita teruji tanpa Firestore/emulator (perilaku SDK Firestore
 * sendiri diverifikasi lewat uji manual multi-device, Day 30).
 */
public class TeamTaskViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private TeamTaskViewModel vm;
    private TeamTaskRepository.TasksCallback tasksCallback;
    private List<TeamTask> latest;

    @Before
    public void setup() {
        TeamTaskRepository repo = mock(TeamTaskRepository.class);
        ArgumentCaptor<TeamTaskRepository.TasksCallback> cap =
                ArgumentCaptor.forClass(TeamTaskRepository.TasksCallback.class);
        when(repo.listenTasks(eq("t1"), cap.capture()))
                .thenReturn(mock(ListenerRegistration.class));
        when(repo.listenOtherChanges(eq("t1"), any()))
                .thenReturn(mock(ListenerRegistration.class));

        vm = new TeamTaskViewModel(repo, "t1");
        tasksCallback = cap.getValue();

        // MediatorLiveData hanya menghitung saat ada observer aktif.
        vm.getTasks().observeForever(tasks -> latest = tasks);
    }

    @Test
    public void defaultShowsAll() {
        tasksCallback.onResult(sample());
        assertEquals(3, latest.size());
    }

    @Test
    public void priorityFilter_keepsOnlyHigh() {
        tasksCallback.onResult(sample());
        vm.setQuery(new TaskQuery(TaskQuery.Filter.PRIORITY_HIGH, null));
        assertEquals(1, latest.size());
        assertEquals("Beta priority", latest.get(0).title);
    }

    @Test
    public void overdueFilter_keepsOnlyLate() {
        tasksCallback.onResult(sample());
        vm.setQuery(new TaskQuery(TaskQuery.Filter.OVERDUE, null));
        assertEquals(1, latest.size());
        assertEquals("Gamma late", latest.get(0).title);
    }

    @Test
    public void keywordSearch_isCaseInsensitiveOnTitle() {
        tasksCallback.onResult(sample());
        vm.setQuery(new TaskQuery(TaskQuery.Filter.ALL, "ALPHA"));
        assertEquals(1, latest.size());
        assertEquals("Alpha", latest.get(0).title);
    }

    @Test
    public void keyword_takesPrecedenceOverFilter() {
        tasksCallback.onResult(sample());
        // Filter prioritas, tapi kata kunci "gamma" → pencarian judul menang.
        vm.setQuery(new TaskQuery(TaskQuery.Filter.PRIORITY_HIGH, "gamma"));
        assertEquals(1, latest.size());
        assertEquals("Gamma late", latest.get(0).title);
    }

    private static List<TeamTask> sample() {
        long day = 86_400_000L;
        return Arrays.asList(
                task("Alpha", Task.PRIORITY_NORMAL, TaskStatus.NOT_STARTED, 0L),
                task("Beta priority", Task.PRIORITY_HIGH, TaskStatus.IN_PROGRESS,
                        System.currentTimeMillis() + day),
                task("Gamma late", Task.PRIORITY_NORMAL, TaskStatus.NOT_STARTED,
                        System.currentTimeMillis() - day));
    }

    private static TeamTask task(String title, int priority, String status, long deadline) {
        TeamTask t = new TeamTask();
        t.title = title;
        t.priority = priority;
        t.status = status;
        t.deadline = deadline;
        return t;
    }
}
