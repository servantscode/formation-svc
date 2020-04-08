package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.formation.Catechist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CatechistDB extends EasyDB<Catechist> {
    private static final Logger LOG = LogManager.getLogger(CatechistDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "s.name");
        FIELD_MAP.put("programId", "program_id");
        FIELD_MAP.put("instructorId", "instructor_id");
        FIELD_MAP.put("roomId", "room_id");
    }

    public CatechistDB() {
        super(Catechist.class, "name", FIELD_MAP);
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("catechists c")
                .leftJoin("people p ON p.id=c.id")
                .leftJoin("programs prog ON prog.id=c.program_id")
                .leftJoin("person_phone_numbers pn ON p.id=pn.person_id AND pn.is_primary")
                .leftJoin("classrooms r ON r.id=c.classroom_id")
                .inOrg("prog.org_id");
    }

    private QueryBuilder data() {
        return select("c.*", "p.name AS name", "r.name AS classroom_name", "p.email", "pn.number AS phone_number");
    }

    public int getCount(String search, int programId) {
        return getCount(select(count())
                .search(searchParser.parse(search))
                .with("c.program_id", programId));
    }

    public List<Catechist> get(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search))
                .with("c.program_id", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Catechist getById(int id) {
        return getOne(select(data()).withId(id));
    }

    public List<Catechist> getProgramCatechists(int programId) {
        return get(select(data()).where("c.program_id=?", programId));
    }

    public List<String> getEmails(String search, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search)).with("program_id", programId);
        return get(query).stream().map(Catechist::getEmail).collect(Collectors.toList());
    }

    public Catechist create(Catechist catechist) {
        InsertBuilder cmd = new InsertBuilder().into("catechists")
                .value("id", catechist.getId())
                .value("program_id", catechist.getProgramId())
                .value("classroom_id", catechist.getClassroomId())
                .value("is_primary", catechist.isPrimary());
        catechist.setId(createAndReturnKey(cmd));
        return catechist;
    }

    public Catechist updateCatechist(Catechist catechist) {
        UpdateBuilder cmd = new UpdateBuilder().update("catechists")
                .value("classroom_id", catechist.getClassroomId())
                .value("is_primary", catechist.isPrimary())
                .with("program_id", catechist.getProgramId())
                .withId(catechist.getId());

        if(!update(cmd))
            throw new RuntimeException("Failed to update catechist with id: " + catechist.getId());

        return catechist;
    }

    public void removeClassroomCatechist(int programId, int classroomId) {
        UpdateBuilder cmd = new UpdateBuilder().update("catechists")
                .value("classroom_id", null)
                .with("program_id", programId)
                .with("classroom_id", classroomId);

        if(!update(cmd))
            throw new RuntimeException("Failed to remove catechist from with classroom id: " + classroomId);
    }

    public Catechist updateClassroomCatechist(Catechist catechist) {
        UpdateBuilder cmd = new UpdateBuilder().update("catechists")
                .value("id", catechist.getId())
                .value("is_primary", catechist.isPrimary())
                .with("program_id", catechist.getProgramId())
                .with("classroom_id", catechist.getClassroomId());

        if(!update(cmd))
            create(catechist);

        return catechist;
    }

    public boolean deleteCatechist(int id) {
        return delete(new DeleteBuilder().delete("catechists").withId(id));
    }

    @Override
    protected Catechist processRow(ResultSet rs) throws SQLException {
        Catechist catechist = new Catechist();
        catechist.setId(rs.getInt("id"));
        catechist.setName(rs.getString("name"));
        catechist.setProgramId(rs.getInt("program_id"));
        catechist.setClassroomId(rs.getInt("classroom_id"));
        catechist.setClassroomName(rs.getString("classroom_name"));
        catechist.setPrimary(rs.getBoolean("is_primary"));
        catechist.setEmail(rs.getString("email"));
        catechist.setPhoneNumber(rs.getString("phone_number"));
        return catechist;
    }
}
