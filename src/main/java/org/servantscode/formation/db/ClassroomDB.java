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

@SuppressWarnings("SqlNoDataSourceInspection")
public class ClassroomDB extends EasyDB<Classroom> {
    private static final Logger LOG = LogManager.getLogger(ClassroomDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "s.name");
        FIELD_MAP.put("programId", "program_id");
        FIELD_MAP.put("instructorId", "instructor_id");
        FIELD_MAP.put("roomId", "room_id");
    }

    public ClassroomDB() {
        super(Classroom.class, "name", FIELD_MAP);
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("classrooms s")
                .leftJoin("people p ON p.id=s.instructor_id")
                .leftJoin("rooms r ON r.id=s.room_id")
                .leftJoin("(SELECT classroom_id, count(classroom_id) AS students FROM registrations GROUP BY classroom_id) reg ON s.id=reg.classroom_id")
                .inOrg("s.org_id");
    }

    private QueryBuilder data() {
        return select("s.*", "p.name AS instructor_name", "r.name AS room_name", "reg.students");
    }

    public int getCount(String search, int programId) {
        return getCount(select(count())
                .search(searchParser.parse(search))
                .with("program_id", programId));
    }

    public List<Classroom> get(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search))
                .with("program_id", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Classroom getById(int id) {
        return getOne(select(data()).where("s.id=?", id));
    }

    public List<Classroom> getProgramClassrooms(int programId) {
        return get(select(data()).where("s.program_id=?", programId));
    }

    public Classroom create(Classroom classroom) {
        InsertBuilder cmd = new InsertBuilder().into("classrooms")
                .value("name", classroom.getName())
                .value("program_id", classroom.getProgramId())
                .value("instructor_id", classroom.getInstructorId())
                .value("room_id", classroom.getRoomId())
                .value("org_id", OrganizationContext.orgId());
        classroom.setId(createAndReturnKey(cmd));
        return classroom;
    }

    public Classroom updateClassroom(Classroom classroom) {
        UpdateBuilder cmd = new UpdateBuilder().update("classrooms")
                .value("name", classroom.getName())
                .value("program_id", classroom.getProgramId())
                .value("instructor_id", classroom.getInstructorId())
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
        s.setRoomId(rs.getInt("room_id"));
        s.setRoomName(rs.getString("room_name"));
        s.setStudentCount(rs.getInt("students"));
        s.setComplete(rs.getBoolean("complete"));
        return s;
    }

}
