package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.commons.security.OrganizationContext;
import org.servantscode.formation.Section;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SqlNoDataSourceInspection")
public class SectionDB extends EasyDB<Section> {
    private static final Logger LOG = LogManager.getLogger(SectionDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "s.name");
        FIELD_MAP.put("programId", "program_id");
        FIELD_MAP.put("instructorId", "instructor_id");
        FIELD_MAP.put("roomId", "room_id");
    }

    public SectionDB() {
        super(Section.class, "name", FIELD_MAP);
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("sections s")
                .leftJoin("people p ON p.id=s.instructor_id")
                .leftJoin("rooms r ON r.id=s.room_id")
                .leftJoin("(SELECT section_id, count(section_id) AS students FROM registrations GROUP BY section_id) reg ON s.id=reg.section_id")
                .inOrg("s.org_id");
    }

    private QueryBuilder data() {
        return select("s.*", "p.name AS instructor_name", "r.name AS room_name", "reg.students");
    }

    public int getCount(String search) {
        return getCount(select(count()).search(searchParser.parse(search)));
    }

    public List<Section> get(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search))
                .where("program_id=?", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Section getById(int id) {
        return getOne(select(data()).where("s.id=?", id));
    }

    public Section create(Section section) {
        InsertBuilder cmd = new InsertBuilder().into("sections")
                .value("name", section.getName())
                .value("program_id", section.getProgramId())
                .value("instructor_id", section.getInstructorId())
                .value("room_id", section.getRoomId())
                .value("org_id", OrganizationContext.orgId());
        section.setId(createAndReturnKey(cmd));
        return section;
    }

    public Section updateSection(Section section) {
        UpdateBuilder cmd = new UpdateBuilder().update("sections")
                .value("name", section.getName())
                .value("program_id", section.getProgramId())
                .value("instructor_id", section.getInstructorId())
                .value("room_id", section.getRoomId())
                .value("complete", section.isComplete())
                .where("id=?", section.getId())
                .where("org_id=?", OrganizationContext.orgId());

        if(!update(cmd))
            throw new RuntimeException("Failed to update section: " + section.getName());

        return section;
    }

    public boolean deleteSection(int id) {
        DeleteBuilder cmd = new DeleteBuilder().delete("sections")
                .withId(id).inOrg();

        return delete(cmd);
    }

    @Override
    protected Section processRow(ResultSet rs) throws SQLException {
        Section s = new Section();
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
