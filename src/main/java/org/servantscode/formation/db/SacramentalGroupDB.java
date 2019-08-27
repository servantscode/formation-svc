package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.*;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.SacramentalGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class SacramentalGroupDB extends EasyDB<SacramentalGroup> {
    private static final Logger LOG = LogManager.getLogger(SacramentalGroupDB.class);

    public SacramentalGroupDB() {
        super(SacramentalGroup.class, "name");
    }

    public int getCount(String search) {
        return getCount(count().from("sacramental_groups").search(searchParser.parse(search)).inOrg());
    }

    public SacramentalGroup getSacramentalGroup(int id) {
        return getOne(selectAll().from("sacramental_groups").withId(id).inOrg());
    }

    public List<SacramentalGroup> getSacramentalGroups(String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("sacramental_groups").search(searchParser.parse(search)).inOrg()
                .page(sortField, start, count);
        return get(query);
    }

    public SacramentalGroup create(SacramentalGroup group) {
        InsertBuilder cmd = insertInto("sacramental_groups")
                .value("name", group.getName())
                .inOrg();
        group.setId(createAndReturnKey(cmd));
        return group;
    }

    public SacramentalGroup updateSacramentalGroup(SacramentalGroup group) {
        UpdateBuilder cmd = update("sacramental_goups")
                .value("name", group.getName())
                .withId(group.getId()).inOrg();
        if (!update(cmd))
            throw new RuntimeException("Could not update sacramental group: " + group.getName());
        return group;
    }

    public boolean deleteSacramentalGroup(int id) {
        DeleteBuilder cmd = deleteFrom("sacramental_groups").withId(id).inOrg();
        return delete(cmd);
    }

    @Override
    protected SacramentalGroup processRow(ResultSet rs) throws SQLException {
        SacramentalGroup sg = new SacramentalGroup();
        sg.setId(rs.getInt("id"));
        sg.setName(rs.getString("name"));
        return sg;
    }
}
