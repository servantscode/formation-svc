package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ProgramDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(ProgramDB.class);

    private SearchParser<Program> searchParser;

    public ProgramDB() {
        this.searchParser = new SearchParser<>(Program.class, "name");
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("programs").search(searchParser.parse(search)).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve program count '" + search + "'", e);
        }
        return 0;
    }

    public Program getProgram(int id) {
        QueryBuilder query = select("prog.*", "p.name as coordinator_name")
                .from("programs prog", "people p")
                .where("prog.coordinator_id = p.id")
                .withId(id).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<Program> programs = processResults(stmt);
            return firstOrNull(programs);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve program: " + id, e);
        }
    }

    public List<Program> getPrograms(String search, String sortField, int start, int count) {
        QueryBuilder query = select("prog.*", "p.name as coordinator_name")
                .from("programs prog", "people p")
                .where("prog.coordinator_id = p.id")
                .search(searchParser.parse(search)).inOrg()
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve programs.", e);
        }
    }

    public Program create(Program program) {
        String sql = "INSERT INTO programs(name, group_id, coordinator_id, org_id) values (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){

            stmt.setString(1, program.getName());
            stmt.setInt(2, program.getGroupId());
            stmt.setInt(3, program.getCoordinatorId());
            stmt.setInt(4, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0) {
                throw new RuntimeException("Could not create program: " + program.getName());
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    program.setId(rs.getInt(1));
            }
            return program;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add program: " + program.getName(), e);
        }
    }

    public Program updateProgram(Program program) {
        String sql = "UPDATE programs SET name=?, group_id=?, coordinator_id=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, program.getName());
            stmt.setInt(2, program.getGroupId());
            stmt.setInt(3, program.getCoordinatorId());
            stmt.setInt(4, program.getId());
            stmt.setInt(5, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update program: " + program.getName());

            return program;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update program: " + program.getName(), e);
        }
    }

    public boolean deleteProgram(int id) {
        String sql = "DELETE FROM programs WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);
            stmt.setInt(2, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete program: " + id, e);
        }
    }

    // ----- Private -----
    private List<Program> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Program> programs = new ArrayList<>();
            while (rs.next()) {
                Program r = new Program();
                r.setId(rs.getInt("id"));
                r.setName(rs.getString("name"));
                r.setGroupId(rs.getInt("group_id"));
                r.setCoordinatorId(rs.getInt("coordinator_id"));
                r.setCoordinatorName(rs.getString("coordinator_name"));
                programs.add(r);
            }
            return programs;
        }
    }
}
