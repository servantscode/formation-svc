package org.servantscode.formation;

import java.util.HashMap;
import java.util.Map;

public class EnrolleeAttendance {
    private int enrolleeId;
    private String enrolleeName;
    private Map<Integer, Boolean> sessionAttendance;

    public EnrolleeAttendance(int enrolleeId, String enrolleeName) {
        this.enrolleeId = enrolleeId;
        this.enrolleeName = enrolleeName;
        this.sessionAttendance = new HashMap<>(32);
    };

    // ----- Accessors -----
    public int getEnrolleeId() { return enrolleeId; }
    public void setEnrolleeId(int enrolleeId) { this.enrolleeId = enrolleeId; }

    public String getEnrolleeName() { return enrolleeName; }
    public void setEnrolleeName(String enrolleeName) { this.enrolleeName = enrolleeName; }

    public Map<Integer, Boolean> getSessionAttendance() { return sessionAttendance; }
    public void setSessionAttendance(Map<Integer, Boolean> sessionAttendance) { this.sessionAttendance = sessionAttendance; }
    public void recordAttendance(int sessionId, boolean attended) { this.sessionAttendance.put(sessionId, attended); }
}
