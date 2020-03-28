package org.servantscode.formation;

public class Catechist {
    private int id;
    private String name;
    private int programId;
    private int classroomId;
    private boolean primary;
    private String classroomName;
    private String email;
    private String phoneNumber;

    public Catechist() { }

    public Catechist(int id, int programId, int classroomId) {
        this(id, programId, classroomId, true);
    }

    public Catechist(int id, int programId, int classroomId, boolean primary) {
        this.id = id;
        this.programId = programId;
        this.classroomId = classroomId;
        this.primary = primary;
    }

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
