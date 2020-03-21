package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.formation.Registration;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class RegistrationDB extends EasyDB<Registration> {
    private static final Logger LOG = LogManager.getLogger(RegistrationDB.class);


    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("enrolleeName", "p.name");
    }

    public RegistrationDB() {
        super(Registration.class, "enrolleeName", FIELD_MAP);
    }

    private QueryBuilder joinTables(QueryBuilder selecton) {
        return selecton.from("registrations r")
                .join("LEFT JOIN people p ON p.id=r.enrollee_id")
                .join("LEFT JOIN sections s ON s.id=r.section_id")
                .join("LEFT JOIN sacramental_groups sg ON sg.id=r.sacramental_group_id");
    }

    private QueryBuilder data() {
        QueryBuilder selections = select("r.*")
                                 .select("p.name AS enrollee_name")
                                 .select(" p.birthdate AS birthdate")
                                 .select("s.name AS section_name")
                                 .select("sg.name AS sg_name");
        return joinTables(selections).inOrg("p.org_id");
    }

    public int getCount(String search, int programId) {
        return getCount(joinTables(count()).search(searchParser.parse(search)).with("r.program_id", programId).inOrg("p.org_id"));
    }

    public Registration getRegistration(int id) {
        return getOne(data().where("r.id=?", id));
    }

    public List<Registration> getRegistrations(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query = data().search(searchParser.parse(search))
                .where("r.program_id=?", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Registration create(Registration registration) {
        InsertBuilder cmd = insertInto("registrations")
                .value("enrollee_id", registration.getEnrolleeId())
                .value("program_id", registration.getProgramId())
                .value("section_id", registration.getSectionId())
                .value("school_grade", registration.getSchoolGrade())
                .value("sacramental_group_id", registration.getSacramentalGroupId());
        registration.setId(createAndReturnKey(cmd));
        return registration;
    }

    public Registration updateRegistration(Registration registration) {
        UpdateBuilder cmd = update("registrations")
                .value("enrollee_id", registration.getEnrolleeId())
                .value("program_id", registration.getProgramId())
                .value("section_id", registration.getSectionId())
                .value("school_grade", registration.getSchoolGrade())
                .value("sacramental_group_id", registration.getSacramentalGroupId())
                .withId(registration.getId());
        if (!update(cmd))
            throw new RuntimeException("Could not update registration: " + registration.getEnrolleeId());
        return registration;
    }

    public boolean deleteRegistration(Registration reg) {
        return delete(deleteFrom("registrations").withId(reg.getId()));
    }

    @Override
    protected Registration processRow(ResultSet rs) throws SQLException {
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
        r.setSchoolGrade(rs.getString("school_grade"));
        r.setSacramentalGroupId(rs.getInt("sacramental_group_id"));
        r.setSacramentalGroupName(rs.getString("sg_name"));
        return r;
    }
}
