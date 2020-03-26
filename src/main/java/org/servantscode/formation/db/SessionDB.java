package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.formation.Session;
import org.servantscode.formation.SessionSeries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class SessionDB extends EasyDB<Session> {
    private static final Logger LOG = LogManager.getLogger(ClassroomDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("startTime", "s.start_time");
        FIELD_MAP.put("endTime", "s.end_time");
    }

    public SessionDB() {
        super(Session.class, "e.title", FIELD_MAP);
    }

    private QueryBuilder baseQuery() {
        return tables(select("s.*", "e.id AS event_id", "e.start_time", "e.end_time"));
    }

    private QueryBuilder tables(QueryBuilder selection) {
        return selection.from("program_sessions s")
                .join("LEFT JOIN events e ON e.id=s.event_id");
    }

    public int getCount(String search, int programId) {
        QueryBuilder query = tables(selectAll())
                .search(searchParser.parse(search))
                .where("program_id=?", programId).inOrg("e.org_id");
        return getCount(query);
    }

    public List<Session> get(String search, String sort, int start, int count, int programId) {
        QueryBuilder query = baseQuery()
                .search(searchParser.parse(search))
                .where("program_id=?", programId).inOrg("e.org_id")
                .page(sort, start, count);
        return get(query);
    }

    public Session getById(int id) {
        QueryBuilder query = baseQuery()
                .where("session_id=?", id).inOrg("e.org_id");
        return getOne(query);
    }

    public void createSeries(SessionSeries series) {
        String sql = "INSERT INTO program_sessions (program_id, event_id) " +
                     "SELECT ?, e.id FROM events e WHERE e.recurring_meeting_id=?";
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, series.getProgramId());
            stmt.setInt(2, series.getRecurrenceId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link event sessions to program.", e);
        }
    }

    public boolean delete(int id) {
        return delete(new DeleteBuilder().delete("program_sessions").withId(id).inOrg());
    }

    // ----- Private -----
    protected Session processRow(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setId(rs.getInt("id"));
        s.setProgramId(rs.getInt("program_id"));
        s.setEventId(rs.getInt("event_id"));
        s.setStartTime(convert(rs.getTimestamp("start_time")));
        s.setEndTime(convert(rs.getTimestamp("end_time")));
        return s;
    }
}
