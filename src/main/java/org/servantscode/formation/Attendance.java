package org.servantscode.formation;

import java.util.LinkedList;
import java.util.List;

public class Attendance {
    private int programId;
    private int classroomId;
    private List<Session> sessions;
    private List<EnrolleeAttendance> attendance;

    public Attendance() {
        this.sessions = new LinkedList<>();
    }

    // ----- Accessors -----
    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public List<Session> getSessions() { return sessions; }
    public void setSessions(List<Session> sessions) { this.sessions = sessions; }
    public void addSession(Session s) { this.sessions.add(s); }

    public List<EnrolleeAttendance> getAttendance() { return attendance; }
    public void setAttendance(List<EnrolleeAttendance> attendance) { this.attendance = attendance; }
}
