package org.servantscode.formation;

public class Section {
    private int id;
    private String name;
    private int programId;
    private int recurrenceId;
    private DayTime dayTime;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getRecurrenceId() { return recurrenceId; }
    public void setRecurrenceId(int recurrenceId) { this.recurrenceId = recurrenceId; }

    public DayTime getDayTime() { return dayTime; }
    public void setDayTime(DayTime dayTime) { this.dayTime = dayTime; }
}
