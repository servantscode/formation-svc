package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Classroom;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ClassroomDB extends EasyDB<Classroom> {
    private static final Logger LOG = LogManager.getLogger(ClassroomDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "class.name");
        FIELD_MAP.put("instructor_name", "p.name");
        FIELD_MAP.put("programId", "class.program_id");
        FIELD_MAP.put("catechistId", "c.id");
        FIELD_MAP.put("roomId", "room_id");
    }

    public ClassroomDB() {
        super(Classroom.class, "name", FIELD_MAP);
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("classrooms class")
                .leftJoin("catechists c ON c.classroom_id=class.id AND c.is_primary")
                .leftJoin("(SELECT classroom_id, string_agg(per.id::TEXT, '|') AS addtl_catechist_ids, string_agg(per.name, '|') as addtl_catechist_names from catechists ca, people per WHERE ca.id = per.id AND NOT ca.is_primary GROUP BY ca.classroom_id)AS ac ON ac.classroom_id = class.id")
                .leftJoin("(SELECT classroom_id, string_agg(per.email, '|') AS catechist_emails from catechists ca, people per WHERE ca.id = per.id GROUP BY ca.classroom_id)AS emails ON emails.classroom_id = class.id")
                .leftJoin("people p ON p.id=c.id")
                .leftJoin("rooms r ON r.id=class.room_id")
                .leftJoin("(SELECT classroom_id, count(classroom_id) AS students FROM registrations GROUP BY classroom_id) reg ON class.id=reg.classroom_id")
                .inOrg("class.org_id");
    }

    private QueryBuilder data() {
        return select("class.*", "c.id AS instructor_id", "p.name AS instructor_name", "r.name AS room_name", "reg.students", "ac.addtl_catechist_ids", "ac.addtl_catechist_names", "catechist_emails");
    }

    public int getCount(String search, int programId) {
        return getCount(select(count())
                .search(searchParser.parse(search))
                .with("class.program_id", programId));
    }

    public List<Classroom> get(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search))
                .with("class.program_id", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Classroom getById(int id) {
        return getOne(select(data()).where("class.id=?", id));
    }

    public List<Classroom> getProgramClassrooms(int programId) {
        return get(select(data()).where("class.program_id=?", programId));
    }

    public Classroom create(Classroom classroom) {
        InsertBuilder cmd = new InsertBuilder().into("classrooms")
                .value("name", classroom.getName())
                .value("program_id", classroom.getProgramId())
                .value("room_id", classroom.getRoomId())
                .value("org_id", OrganizationContext.orgId());
        classroom.setId(createAndReturnKey(cmd));
        return classroom;
    }

    public Classroom updateClassroom(Classroom classroom) {
        UpdateBuilder cmd = new UpdateBuilder().update("classrooms")
                .value("name", classroom.getName())
                .value("program_id", classroom.getProgramId())
                .value("room_id", classroom.getRoomId())
                .value("complete", classroom.isComplete())
                .where("id=?", classroom.getId())
                .where("org_id=?", OrganizationContext.orgId());

        if(!update(cmd))
            throw new RuntimeException("Failed to update classroom: " + classroom.getName());

        return classroom;
    }

    public boolean deleteClassroom(int id) {
        DeleteBuilder cmd = new DeleteBuilder().delete("classrooms")
                .withId(id).inOrg();

        return delete(cmd);
    }

    @Override
    protected Classroom processRow(ResultSet rs) throws SQLException {
        Classroom s = new Classroom();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setProgramId(rs.getInt("program_id"));
        s.setInstructorId(rs.getInt("instructor_id"));
        s.setInstructorName(rs.getString("instructor_name"));
        s.setAdditionalInstructorIds(decodeList(rs.getString("addtl_catechist_ids"), Integer::parseInt));
        s.setAdditionalInstructorNames(decodeList(rs.getString("addtl_catechist_names")));
        s.setInstructorEmails(decodeList(rs.getString("catechist_emails")));
        s.setRoomId(rs.getInt("room_id"));
        s.setRoomName(rs.getString("room_name"));
        s.setStudentCount(rs.getInt("students"));
        s.setComplete(rs.getBoolean("complete"));
        return s;
    }
}
