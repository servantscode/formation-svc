package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ProgramDB extends EasyDB<Program> {
    private static final Logger LOG = LogManager.getLogger(ProgramDB.class);

    private static HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "prog.name");
        FIELD_MAP.put("coordinatorId", "coordinator_id");
        FIELD_MAP.put("groupId", "group_id");
    }

    public ProgramDB() {
        super(Program.class, "name", FIELD_MAP);
    }

    private QueryBuilder data(QueryBuilder query) {
        return query.from("programs prog").leftJoin("people p ON prog.coordinator_id = p.id");
    }

    private QueryBuilder baseQuery() {
        return data(select("prog.*", "p.name as coordinator_name", "students"))
                .join("LEFT JOIN (SELECT program_id, count(program_id) AS students FROM registrations r GROUP BY program_id) reg ON prog.id=reg.program_id")
                .inOrg("prog.org_id");
    }

    public int getCount(String search) {
        return getCount(data(count()).search(searchParser.parse(search)).inOrg("prog.org_id"));
    }

    public Program getProgram(int id) {
        return getOne(baseQuery().where("prog.id=?", id));
    }

    public List<Program> getPrograms(String search, String sortField, int start, int count) {
        QueryBuilder query = baseQuery()
                .search(searchParser.parse(search))
                .page(sortField, start, count);
        return get(query);
    }

    public Program create(Program program) {
        InsertBuilder cmd = insertInto("programs")
                .value("name", program.getName())
                .value("group_id", program.getGroupId())
                .value("coordinator_id", program.getCoordinatorId())
                .inOrg();

        program.setId(createAndReturnKey(cmd));
        return program;
    }

    public Program updateProgram(Program program) {
        UpdateBuilder cmd = update("programs")
                .value("name", program.getName())
                .value("group_id", program.getGroupId())
                .value("coordinator_id", program.getCoordinatorId())
                .withId(program.getId()).inOrg();

        if(!update(cmd))
            throw new RuntimeException("Could not update program: " + program.getName());

        return program;
    }

    public boolean deleteProgram(int id) {
        return delete(deleteFrom("programs").withId(id).inOrg());
    }

    @Override
    protected Program processRow(ResultSet rs) throws SQLException {
        Program p = new Program();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setGroupId(rs.getInt("group_id"));
        p.setCoordinatorId(rs.getInt("coordinator_id"));
        p.setCoordinatorName(rs.getString("coordinator_name"));
        p.setRegistrations(rs.getInt("students"));
        return p;
    }
}
