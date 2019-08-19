package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.ProgramGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ProgramGroupDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(ProgramGroupDB.class);

    private SearchParser<ProgramGroup> searchParser;

    public ProgramGroupDB() {
        this.searchParser = new SearchParser<>(ProgramGroup.class, "name");
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("program_groups").search(searchParser.parse(search)).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve group count '" + search + "'", e);
        }
        return 0;
    }

    public ProgramGroup getProgramGroup(int id) {
        QueryBuilder query = selectAll().from("program_groups").withId(id).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<ProgramGroup> groups = processResults(stmt);
            return firstOrNull(groups);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve group: " + id, e);
        }
    }

    public List<ProgramGroup> getProgramGroups(String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("program_groups").search(searchParser.parse(search)).inOrg()
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve groups.", e);
        }
    }

    public ProgramGroup create(ProgramGroup group) {
        String sql = "INSERT INTO program_groups(name, org_id) values (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){

            stmt.setString(1, group.getName());
            stmt.setInt(2, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0) {
                throw new RuntimeException("Could not create program group: " + group.getName());
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    group.setId(rs.getInt(1));
            }
            return group;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add program group: " + group.getName(), e);
        }
    }

    public ProgramGroup updateProgramGroup(ProgramGroup group) {
        String sql = "UPDATE program_groups SET name=?, complete=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, group.getName());
            stmt.setBoolean(2, group.isComplete());
            stmt.setInt(3, group.getId());
            stmt.setInt(4, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update program group: " + group.getName());

            return group;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update program group: " + group.getName(), e);
        }
    }

    public boolean deleteProgramGroup(int id) {
        String sql = "DELETE FROM program_groups WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);
            stmt.setInt(2, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete group: " + id, e);
        }
    }

    // ----- Private -----
    private List<ProgramGroup> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<ProgramGroup> groups = new ArrayList<>();
            while (rs.next()) {
                ProgramGroup group = new ProgramGroup();
                group.setId(rs.getInt("id"));
                group.setName(rs.getString("name"));
                group.setComplete(rs.getBoolean("complete"));
                groups.add(group);
            }
            return groups;
        }
    }
}
