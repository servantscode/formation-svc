package org.servantscode.formation;

import java.util.List;

public class Classroom {
    private int id;
    private String name;
    private int programId;
    private int instructorId;
    private String instructorName;
    private List<Integer> additionalInstructorIds;
    private List<String> additionalInstructorNames;
    private List<String> instructorEmails;
    private int roomId;
    private String roomName;
    private int studentCount;
    private boolean complete;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public List<Integer> getAdditionalInstructorIds() { return additionalInstructorIds; }
    public void setAdditionalInstructorIds(List<Integer> additionalInstructorIds) { this.additionalInstructorIds = additionalInstructorIds; }

    public List<String> getAdditionalInstructorNames() { return additionalInstructorNames; }
    public void setAdditionalInstructorNames(List<String> additionalInstructorNames) { this.additionalInstructorNames = additionalInstructorNames; }

    public List<String> getInstructorEmails() { return instructorEmails; }
    public void setInstructorEmails(List<String> instructorEmails) { this.instructorEmails = instructorEmails; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
}
