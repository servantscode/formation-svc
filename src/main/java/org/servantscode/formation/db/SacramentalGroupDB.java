package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.SacramentalGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class SacramentalGroupDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(SacramentalGroupDB.class);

    private SearchParser<SacramentalGroup> searchParser;

    public SacramentalGroupDB() {
        this.searchParser = new SearchParser<>(SacramentalGroup.class, "name");
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("sacramental_groups").search(searchParser.parse(search)).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve sacramental group count '" + search + "'", e);
        }
        return 0;
    }

    public SacramentalGroup getSacramentalGroup(int id) {
        QueryBuilder query = selectAll().from("sacramental_groups").withId(id).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<SacramentalGroup> sacramental_groups = processResults(stmt);
            return firstOrNull(sacramental_groups);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve sacramental group: " + id, e);
        }
    }

    public List<SacramentalGroup> getSacramentalGroups(String search, String sortField, int start, int count) {
        QueryBuilder query = selectAll().from("sacramental_groups").search(searchParser.parse(search)).inOrg()
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve sacramental groups.", e);
        }
    }

    public SacramentalGroup create(SacramentalGroup sacramental_group) {
        String sql = "INSERT INTO sacramental_groups(name, org_id) values (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){

            stmt.setString(1, sacramental_group.getName());
            stmt.setInt(2, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0) {
                throw new RuntimeException("Could not create sacramental group: " + sacramental_group.getName());
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    sacramental_group.setId(rs.getInt(1));
            }
            return sacramental_group;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add sacramental group: " + sacramental_group.getName(), e);
        }
    }

    public SacramentalGroup updateSacramentalGroup(SacramentalGroup sacramental_group) {
        String sql = "UPDATE sacramental_groups SET name=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, sacramental_group.getName());
            stmt.setInt(2, sacramental_group.getId());
            stmt.setInt(3, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update sacramental group: " + sacramental_group.getName());

            return sacramental_group;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update sacramental group: " + sacramental_group.getName(), e);
        }
    }

    public boolean deleteSacramentalGroup(int id) {
        String sql = "DELETE FROM sacramental_groups WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);
            stmt.setInt(2, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete sacramental group: " + id, e);
        }
    }

    // ----- Private -----
    private List<SacramentalGroup> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<SacramentalGroup> groups = new ArrayList<>();
            while (rs.next()) {
                SacramentalGroup sg = new SacramentalGroup();
                sg.setId(rs.getInt("id"));
                sg.setName(rs.getString("name"));
                groups.add(sg);
            }
            return groups;
        }
    }
}
