package com.example.taskreminder2.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit test logika overdue (Fitur-08). Memakai overload {@code now} eksplisit. */
public class OverdueCheckerTest {

    private static final long NOW = 2000L;

    @Test
    public void overdue_pastDeadlineAndNotDone() {
        assertTrue(OverdueChecker.isOverdue(1000L, TaskStatus.NOT_STARTED, NOW));
    }

    @Test
    public void notOverdue_whenDone() {
        assertFalse(OverdueChecker.isOverdue(1000L, TaskStatus.DONE, NOW));
    }

    @Test
    public void notOverdue_whenNoDeadline() {
        // deadline 0 = tanpa deadline, tidak pernah terlambat (guard penting).
        assertFalse(OverdueChecker.isOverdue(0L, TaskStatus.NOT_STARTED, NOW));
    }

    @Test
    public void notOverdue_whenDeadlineInFuture() {
        assertFalse(OverdueChecker.isOverdue(5000L, TaskStatus.NOT_STARTED, NOW));
    }

    @Test
    public void notOverdue_whenDeadlineEqualsNow() {
        // perbandingan strict (<), jadi tepat di now belum terlambat.
        assertFalse(OverdueChecker.isOverdue(NOW, TaskStatus.NOT_STARTED, NOW));
    }
}
