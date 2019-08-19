package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Section;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class SectionDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(SectionDB.class);

    private SearchParser<Section> searchParser;

    public SectionDB() {
        this.searchParser = new SearchParser<>(Section.class, "name");
    }

    public int getCount(String search) {
        QueryBuilder query = count().from("sections").search(searchParser.parse(search)).inOrg();
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve section count '" + search + "'", e);
        }
        return 0;
    }

    private QueryBuilder baseQuery() {
        return select("s.*", "p.name", "r.name", "count(reg.section_id) AS students")
                .from("sections s", "people p", "rooms r", "registrations reg")
                .where("p.id = s.instructor_id").where("r.id = s.room_id").where("reg.section_id=s.id");
    }

    public Section getSection(int id) {
        QueryBuilder query = baseQuery().withId(id).inOrg().groupBy("reg.section_id");
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<Section> sections = processResults(stmt);
            return firstOrNull(sections);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve section: " + id, e);
        }
    }


    public List<Section> getSections(String search, String sortField, int start, int count) {
        QueryBuilder query = baseQuery().search(searchParser.parse(search)).inOrg()
                .groupBy("reg.section_id").sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve sections.", e);
        }
    }

    public Section create(Section section) {
        String sql = "INSERT INTO sections(name, program_id, instructor_id, room_id, org_id) values (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){

            stmt.setString(1, section.getName());
            stmt.setInt(2, section.getProgramId());
            stmt.setInt(3, section.getInstructorId());
            stmt.setInt(4, section.getRoomId());
            stmt.setInt(5, OrganizationContext.orgId());

            if(stmt.executeUpdate() == 0) {
                throw new RuntimeException("Could not create section: " + section.getName());
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    section.setId(rs.getInt(1));
            }
            return section;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add section: " + section.getName(), e);
        }
    }

    public Section updateSection(Section section) {
        String sql = "UPDATE sections SET name=?, group_id=?, instructor_id=?, room_id=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, section.getName());
            stmt.setInt(2, section.getProgramId());
            stmt.setInt(3, section.getInstructorId());
            stmt.setInt(4, section.getRoomId());
            stmt.setInt(5, section.getId());
            stmt.setInt(6, OrganizationContext.orgId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update section: " + section.getName());

            return section;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update section: " + section.getName(), e);
        }
    }

    public boolean deleteSection(int id) {
        String sql = "DELETE FROM sections WHERE id=? AND org_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, id);
            stmt.setInt(2, OrganizationContext.orgId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete section: " + id, e);
        }
    }

    // ----- Private -----
    private List<Section> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Section> sections = new ArrayList<>();
            while (rs.next()) {
                Section s = new Section();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setProgramId(rs.getInt("program_id"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setInstructorName(rs.getString("instructor_name"));
                s.setRoomId(rs.getInt("room_id"));
                s.setRoomName(rs.getString("room_name"));
                s.setStudentCount(rs.getInt("students"));
                sections.add(s);
            }
            return sections;
        }
    }
}
