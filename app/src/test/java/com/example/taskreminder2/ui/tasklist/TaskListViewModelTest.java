package com.example.taskreminder2.ui.tasklist;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.taskreminder2.data.local.entity.Task;
import com.example.taskreminder2.data.repository.TaskRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit test {@link TaskListViewModel}: memverifikasi switchMap memilih sumber
 * LiveData yang benar (Fitur-03) tanpa Activity/emulator. Repository di-mock.
 */
public class TaskListViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantRule = new InstantTaskExecutorRule();

    private TaskRepository repo;
    private TaskListViewModel vm;

    @Before
    public void setup() {
        repo = mock(TaskRepository.class);
        when(repo.getAllTasks()).thenReturn(listOf("all"));
        when(repo.filterByPriority(1)).thenReturn(listOf("prio"));
        when(repo.filterOverdue(anyLong())).thenReturn(listOf("late"));
        when(repo.searchByTitle("kw")).thenReturn(listOf("found"));
        vm = new TaskListViewModel(repo);
    }

    @Test
    public void defaultShowsAll() {
        assertEquals("all", firstTitle(vm.getTasks()));
    }

    @Test
    public void priorityFilterSwitchesSource() {
        vm.setQuery(TaskListViewModel.Filter.PRIORITY_HIGH, null);
        assertEquals("prio", firstTitle(vm.getTasks()));
    }

    @Test
    public void overdueFilterSwitchesSource() {
        vm.setQuery(TaskListViewModel.Filter.OVERDUE, null);
        assertEquals("late", firstTitle(vm.getTasks()));
    }

    @Test
    public void searchTakesPrecedenceOverFilter() {
        vm.setQuery(TaskListViewModel.Filter.PRIORITY_HIGH, "kw");
        assertEquals("found", firstTitle(vm.getTasks()));
    }

    @Test
    public void blankKeywordFallsBackToFilter() {
        vm.setQuery(TaskListViewModel.Filter.PRIORITY_HIGH, "   ");
        assertEquals("prio", firstTitle(vm.getTasks()));
    }

    private static LiveData<List<Task>> listOf(String title) {
        Task t = new Task();
        t.title = title;
        return new MutableLiveData<>(Collections.singletonList(t));
    }

    /** Mengambil judul item pertama dari LiveData (sinkron via InstantTaskExecutorRule). */
    private static String firstTitle(LiveData<List<Task>> live) {
        final String[] holder = new String[1];
        Observer<List<Task>> observer = value -> {
            if (value != null && !value.isEmpty()) {
                holder[0] = value.get(0).title;
            }
        };
        live.observeForever(observer);
        live.removeObserver(observer);
        return holder[0];
    }
}
