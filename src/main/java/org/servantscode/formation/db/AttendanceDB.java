package org.servantscode.formation.db;

import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.formation.Attendance;
import org.servantscode.formation.EnrolleeAttendance;
import org.servantscode.formation.SessionAttendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttendanceDB extends DBAccess {

    private final SessionDB sessionDB;

    public AttendanceDB() {
        sessionDB = new SessionDB();
    }

    public Attendance getClassroomAttendance(int classroomId) {
        try(Connection conn = getConnection()) {
            Attendance a = getAttendanceAndSessions(classroomId, conn);
            if(a != null)
                a.setAttendance(getSessionAttendance(classroomId, conn));
            return a;
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve classroom attendance.", e);
        }
    }

    public void upsertSessionAttendance(SessionAttendance attendance) {
        String sql = "INSERT INTO attendance (enrollee_id, classroom_id, session_id, attendance) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (enrollee_id, classroom_id, session_id) " +
                "DO UPDATE SET attendance=EXCLUDED.attendance";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(2, attendance.getClassroomId());
            stmt.setInt(3, attendance.getSessionId());
            for(Map.Entry<Integer, Boolean> entries: attendance.getEnrolleeAttendance().entrySet()) {
                stmt.setInt(1, entries.getKey());
                stmt.setBoolean(4, entries.getValue());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Could not record attendance data.", e);
        }
    }

    // ----- Private -----
    private Attendance getAttendanceAndSessions(int classroomId, Connection conn) throws SQLException {
        QueryBuilder query = select("s.*", "sec.id AS classroom_id", "sec.program_id AS program_id", "e.start_time", "e.end_time")
                .from("program_sessions s")
                .leftJoin("classrooms sec ON s.program_id=sec.program_id")
                .leftJoin("events e ON s.event_id=e.id")
                .where("sec.id=?", classroomId).inOrg("sec.org_id")
                .sort("e.start_time");
        try (PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            Attendance a = null;
            while(rs.next()) {
                if(a == null) {
                    a = new Attendance();
                    a.setProgramId(rs.getInt("program_id"));
                    a.setClassroomId(rs.getInt("classroom_id"));
                }

                a.addSession(sessionDB.processRow(rs));
            }
            return a;
        }
    }

    private ArrayList<EnrolleeAttendance> getSessionAttendance(int classroomId, Connection conn) throws SQLException {
        QueryBuilder query = select("r.enrollee_id", "a.session_id", "a.attendance", "p.name AS enrollee_name")
                .from("registrations r")
                .leftJoin("attendance a on r.enrollee_id=a.enrollee_id")
                .leftJoin("people p ON p.id=r.enrollee_id")
                .where("r.classroom_id=?", classroomId)
                .inOrg("p.org_id");
        try (PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            Map<Integer, EnrolleeAttendance> aMap = new HashMap<>(32);
            while (rs.next()) {
                int enrolleeId = rs.getInt("enrollee_id");
                if (!aMap.containsKey(enrolleeId))
                    aMap.put(enrolleeId, new EnrolleeAttendance(enrolleeId, rs.getString("enrollee_name")));

                int sessionId = rs.getInt("session_id");
                if(sessionId > 0)
                    aMap.get(enrolleeId).recordAttendance(sessionId, rs.getBoolean("attendance"));
            }

            ArrayList<EnrolleeAttendance> ea = new ArrayList<>(aMap.size());
            ea.addAll(aMap.values());
            return ea;
        }
    }
}
