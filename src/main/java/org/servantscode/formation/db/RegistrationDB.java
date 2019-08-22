package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.DBAccess;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.SearchParser;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Registration;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class RegistrationDB extends DBAccess {
    private static final Logger LOG = LogManager.getLogger(RegistrationDB.class);

    private SearchParser<Registration> searchParser;

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("enrolleeName", "enrollee_name");
    }


    public RegistrationDB() {
        this.searchParser = new SearchParser<>(Registration.class, "enrollee_name", FIELD_MAP);
    }

    public int getCount(String search) {
        QueryBuilder query = count()
                .from("registrations r")
                .join("LEFT JOIN people p ON p.id=r.enrollee_id")
                .join("LEFT JOIN sections s ON s.id=r.section_id")
                .join("LEFT JOIN sacramental_groups sg ON sg.id=r.sacramental_group_id")
                .inOrg("p.org_id")
                .search(searchParser.parse(search));
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve people count '" + search + "'", e);
        }
        return 0;
    }

    public Registration getRegistration(int id) {
        QueryBuilder query = baseQuery().where("r.id=?", id);
        try (Connection conn = getConnection();
             PreparedStatement stmt = query.prepareStatement(conn);
        ) {
            List<Registration> registrations = processResults(stmt);
            return firstOrNull(registrations);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve registration: " + id, e);
        }
    }

    private QueryBuilder baseQuery() {
        return select("r.*", "p.name AS enrollee_name", " p.birthdate AS birthdate", "s.name AS section_name", "sg.name AS sg_name")
                .from("registrations r")
                .join("LEFT JOIN people p ON p.id=r.enrollee_id")
                .join("LEFT JOIN sections s ON s.id=r.section_id")
                .join("LEFT JOIN sacramental_groups sg ON sg.id=r.sacramental_group_id")
                .inOrg("p.org_id");
    }

    public List<Registration> getRegistrations(String search, String sortField, int start, int count) {
        QueryBuilder query = baseQuery().search(searchParser.parse(search))
                .sort(sortField).limit(count).offset(start);
        try ( Connection conn = getConnection();
              PreparedStatement stmt = query.prepareStatement(conn)
        ) {
            return processResults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve registrations.", e);
        }
    }

    public Registration create(Registration registration) {
        String sql = "INSERT INTO registrations(enrollee_id, program_id, section_id, grade, sacramental_group_id) values (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){

            stmt.setInt(1, registration.getEnrolleeId());
            stmt.setInt(2, registration.getProgramId());
            stmt.setInt(3, registration.getSectionId());
            stmt.setInt(4, registration.getGrade());
            if(registration.getSacramentalGroupId() > 0) {
                stmt.setInt(5, registration.getSacramentalGroupId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if(stmt.executeUpdate() == 0) {
                throw new RuntimeException("Could not create registration: " + registration.getEnrolleeId());
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    registration.setId(rs.getInt(1));
            }
            return registration;
        } catch (SQLException e) {
            throw new RuntimeException("Could not add registration: " + registration.getEnrolleeId(), e);
        }
    }

    public Registration updateRegistration(Registration registration) {
        String sql = "UPDATE registrations SET enrollee_id=?, program_id=?, section_id=?, grade=?, sacramental_group_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, registration.getEnrolleeId());
            stmt.setInt(2, registration.getProgramId());
            stmt.setInt(3, registration.getSectionId());
            stmt.setInt(4, registration.getGrade());
            if(registration.getSacramentalGroupId() > 0) {
                stmt.setInt(5, registration.getSacramentalGroupId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, registration.getId());

            if (stmt.executeUpdate() == 0)
                throw new RuntimeException("Could not update registration: " + registration.getEnrolleeId());

            return registration;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update registration: " + registration.getEnrolleeId(), e);
        }
    }

    public boolean deleteRegistration(Registration reg) {
        String sql = "DELETE FROM registrations WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {

            stmt.setInt(1, reg.getId());
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete registration for: " + reg.getEnrolleeId(), e);
        }
    }

    // ----- Private -----
    private List<Registration> processResults(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Registration> registrations = new ArrayList<>();
            while (rs.next()) {
                Registration r = new Registration();
                r.setId(rs.getInt("id"));
                r.setEnrolleeId(rs.getInt("enrollee_id"));
                r.setEnrolleeName(rs.getString("enrollee_name"));
                r.setProgramId(rs.getInt("program_id"));
                r.setSectionId(rs.getInt("section_id"));
                r.setSectionName(rs.getString("section_name"));
                ZonedDateTime birthdate = convert(rs.getTimestamp("birthdate"));
                ZonedDateTime today = ZonedDateTime.now();
                int years = today.getYear() - birthdate.getYear();
                if( today.getDayOfYear() < birthdate.getDayOfYear()) years--;
                r.setEnrolleeAge(years);
                r.setGrade(rs.getInt("grade"));
                r.setSacramentalGroupId(rs.getInt("sacramental_group_id"));
                r.setSacramentalGroupName(rs.getString("sg_name"));
                registrations.add(r);
            }
            return registrations;
        }
    }
}
