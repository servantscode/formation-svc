package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.formation.Registration;
import org.servantscode.formation.Student;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SqlNoDataSourceInspection")
public class StudentDB extends EasyDB<Student> {
    private static final Logger LOG = LogManager.getLogger(StudentDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("enrolleeName", "p.name");
    }

    public StudentDB() {
        super(Student.class, "enrolleeName", FIELD_MAP);
    }

    private QueryBuilder joinTables(QueryBuilder selection) {
        return selection.from("registrations r")
                .leftJoin("people p ON p.id=r.enrollee_id")
                .leftJoin("relationships rel ON rel.subject_id=p.id AND rel.contact_preference=1 AND rel.guardian=true")
                .leftJoin("people parent ON rel.other_id=parent.id")
                .leftJoin("person_phone_numbers pn ON pn.person_id=parent.id AND pn.is_primary=true")
                .leftJoin("sections s ON s.id=r.section_id")
                .leftJoin("sacramental_groups sg ON sg.id=r.sacramental_group_id")
                .inOrg("p.org_id");
    }

    private QueryBuilder data() {
        QueryBuilder selections = select("r.*")
                .select("p.name AS enrollee_name")
                .select("p.birthdate AS birthdate")
                .select("p.allergies")
                .select("s.name AS section_name")
                .select("sg.name AS sg_name")
                .select("string_agg(parent.name, '|') AS parents")
                .select("string_agg(pn.number, '|') AS parent_phones");
        return joinTables(selections).inOrg("p.org_id");
    }

    public Map<Integer, List<Student>> getProgramClassAssignments(int programId) {
        QueryBuilder query = data().groupBy("r.id", "p.id", "s.id", "sg.id", "s.id");
        Map<Integer, List<Student>> results = new HashMap<>();

        get(query).forEach(r -> {
            if(!results.containsKey(r.getSectionId()))
                results.put(r.getSectionId(), new LinkedList<>());
            results.get(r.getSectionId()).add(r);
        });

        return results;
    }

    @Override
    protected Student processRow(ResultSet rs) throws SQLException {
        Student r = new Student();
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
        r.setParentNames(decodeList(rs.getString("parents")));
        r.setParentPhones(decodeList(rs.getString("parent_phones")));
        r.setAllergies(decodeList(rs.getString("allergies")));
        return r;
    }
}
