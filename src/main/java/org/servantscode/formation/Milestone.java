package org.servantscode.formation;

public class Milestone {
    private int id;
    private int classroomId;
    private String name;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
