package org.servantscode.formation;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class DayTimeTest {
    @Test
    public void testGetDayTime() {
        DayTime dt = new DayTime();
        dt.setStartTimeHours(2);
        dt.setStartTimeMinutes(5);
        assertEquals("DayTime not formatted correctly.", "2:05", dt.getTimeOfDay());
    }

    @Test
    public void testGetDayTimePM() {
        DayTime dt = new DayTime();
        dt.setStartTimeHours(14);
        dt.setStartTimeMinutes(5);
        assertEquals("DayTime not formatted correctly.", "14:05", dt.getTimeOfDay());
    }


    @Test
    public void testGetDayTimeNoMinutes() {
        DayTime dt = new DayTime();
        dt.setStartTimeHours(2);
        assertEquals("DayTime not formatted correctly.", "2:00", dt.getTimeOfDay());
    }

    @Test
    public void testSetDayTimeNoMinutes() {
        DayTime dt = new DayTime();
        dt.setTimeOfDay("14");
        assertEquals("DayTime hour not set correctly.", 14, dt.getStartTimeHours());
        assertEquals("DayTime minute not set correctly.", 0, dt.getStartTimeMinutes());
    }
}
