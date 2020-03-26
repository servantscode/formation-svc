package org.servantscode.formation;

import java.util.Map;

public class SessionAttendance {
    private int programId;
    private int classroomId;
    private int sessionId;
    private Map<Integer, Boolean> enrolleeAttendance;

    public SessionAttendance() {}

    // ----- Accessors -----
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public Map<Integer, Boolean> getEnrolleeAttendance() { return enrolleeAttendance; }
    public void setEnrolleeAttendance(Map<Integer, Boolean> enrolleeAttendance) { this.enrolleeAttendance = enrolleeAttendance; }
}
