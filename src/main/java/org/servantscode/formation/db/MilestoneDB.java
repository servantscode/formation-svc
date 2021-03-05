package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.formation.Section;
import org.servantscode.formation.Milestone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class MilestoneDB extends EasyDB<Milestone> {
    private static final Logger LOG = LogManager.getLogger(MilestoneDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
    }

    public MilestoneDB() {
        super(Milestone.class, "m.name", FIELD_MAP);
    }

    private QueryBuilder baseQuery() {
        return tables(select("*"));
    }

    private QueryBuilder tables(QueryBuilder selection) {
        return selection.from("milestones");
    }

    public int getCount(String search, int classroomId) {
        QueryBuilder query = tables(selectAll())
                .search(searchParser.parse(search))
                .with("classroom_id", classroomId).inOrg();
        return getCount(query);
    }

    public List<Milestone> get(String search, String sort, int start, int count, int classroomId) {
        QueryBuilder query = baseQuery()
                .search(searchParser.parse(search))
                .with("classroom_id", classroomId).inOrg()
                .page(sort, start, count);
        return get(query);
    }

    public Milestone getById(int id) {
        return getOne(baseQuery().withId(id).inOrg());
    }

    public void create(Milestone m) {
        InsertBuilder cmd = insertInto("milestones")
                .value("classroom_id", m.getClassroomId())
                .value("name", m.getName())
                .inOrg();

        m.setId(createAndReturnKey(cmd));
    }

    public void update(Milestone m) {
        UpdateBuilder cmd = update("milestones")
                .value("classroom_id", m.getClassroomId())
                .value("name", m.getName())
                .withId(m.getId()).inOrg();

        if(!update(cmd))
            throw new RuntimeException("Could not update milestone: " + m.getName());
    }

    public boolean delete(int id) {
        return delete(new DeleteBuilder().delete("milestones").withId(id).inOrg());
    }

    // ----- Private -----
    protected Milestone processRow(ResultSet rs) throws SQLException {
        Milestone s = new Milestone();
        s.setId(rs.getInt("id"));
        s.setClassroomId(rs.getInt("classroom_id"));
        s.setName(rs.getString("name"));
        return s;
    }
}
