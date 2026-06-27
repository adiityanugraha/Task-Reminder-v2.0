package com.example.taskreminder2.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit test konstanta & label status. */
public class TaskStatusTest {

    @Test
    public void indexOf_knownValues() {
        assertEquals(0, TaskStatus.indexOf(TaskStatus.NOT_STARTED));
        assertEquals(1, TaskStatus.indexOf(TaskStatus.IN_PROGRESS));
        assertEquals(2, TaskStatus.indexOf(TaskStatus.DONE));
    }

    @Test
    public void indexOf_unknownReturnsMinusOne() {
        assertEquals(-1, TaskStatus.indexOf("TIDAK_ADA"));
    }

    @Test
    public void label_mapsCanonicalToFriendly() {
        assertEquals("Belum Mulai", TaskStatus.label(TaskStatus.NOT_STARTED));
        assertEquals("Sedang Dikerjakan", TaskStatus.label(TaskStatus.IN_PROGRESS));
        assertEquals("Selesai", TaskStatus.label(TaskStatus.DONE));
    }

    @Test
    public void label_unknownReturnsInput() {
        assertEquals("XYZ", TaskStatus.label("XYZ"));
    }
}
