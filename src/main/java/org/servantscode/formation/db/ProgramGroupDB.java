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
import org.servantscode.formation.ProgramGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ProgramGroupDB extends EasyDB<ProgramGroup> {
    private static final Logger LOG = LogManager.getLogger(ProgramGroupDB.class);

    public ProgramGroupDB() {
        super(ProgramGroup.class, "name");
    }

    public int getCount(String search) {
        return getCount(count().from("program_groups").search(searchParser.parse(search)).inOrg());
    }

    public ProgramGroup getProgramGroup(int id) {
        return getOne(selectAll().from("program_groups").withId(id).inOrg());
    }

    public List<ProgramGroup> getProgramGroups(String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("program_groups").search(searchParser.parse(search)).inOrg()
                .page(sortField, start, count);
        return get(query);
    }

    public ProgramGroup create(ProgramGroup group) {
        InsertBuilder cmd = insertInto("program_groups")
                .value("name", group.getName())
                .inOrg();
        group.setId(createAndReturnKey(cmd));
        return group;
    }

    public ProgramGroup updateProgramGroup(ProgramGroup group) {
        UpdateBuilder cmd = update("program_groups")
                .value("name", group.getName())
                .value("complete", group.isComplete())
                .withId(group.getId()).inOrg();

        if (!update(cmd))
            throw new RuntimeException("Could not update program group: " + group.getName());
        return group;
    }

    public boolean deleteProgramGroup(int id) {
        return delete(deleteFrom("program_groups").withId(id).inOrg());
    }

    @Override
    protected ProgramGroup processRow(ResultSet rs) throws SQLException {
        ProgramGroup group = new ProgramGroup();
        group.setId(rs.getInt("id"));
        group.setName(rs.getString("name"));
        group.setComplete(rs.getBoolean("complete"));
        return group;
    }
}
