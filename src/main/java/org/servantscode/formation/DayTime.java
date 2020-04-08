package org.servantscode.formation;

import java.time.DayOfWeek;

public class DayTime {
    private DayOfWeek dayOfWeek;
    private int startTimeHours;
    private int startTimeMinutes;

    // ----- Accessors -----
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getStartTimeHours() { return startTimeHours; }
    public void setStartTimeHours(int startTimeHours) { this.startTimeHours = startTimeHours; }

    public int getStartTimeMinutes() { return startTimeMinutes; }
    public void setStartTimeMinutes(int startTimeMinutes) { this.startTimeMinutes = startTimeMinutes; }

    public String getTimeOfDay() {
        return String.format("%d:%02d", startTimeHours, startTimeMinutes);
    }

    public void setTimeOfDay(String timeOfDay) {
        String[] bits = timeOfDay.split(":");
        this.startTimeHours = Integer.parseInt(bits[0]);
        if(bits.length > 1)
            this.startTimeMinutes = Integer.parseInt(bits[1]);
    }
}
