package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.EasyDB;
import org.servantscode.commons.search.DeleteBuilder;
import org.servantscode.commons.search.InsertBuilder;
import org.servantscode.commons.search.QueryBuilder;
import org.servantscode.commons.search.UpdateBuilder;
import org.servantscode.formation.DayTime;
import org.servantscode.formation.Section;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SectionDB extends EasyDB<Section> {
    private static final Logger LOG = LogManager.getLogger(SectionDB.class);

    private static final HashMap<String, String> FIELD_MAP = new HashMap<>(8);
    static {
        FIELD_MAP.put("name", "s.name");
        FIELD_MAP.put("programId", "program_id");
    }

    public SectionDB() {
        super(Section.class, "name", FIELD_MAP);
    }

    private QueryBuilder select(QueryBuilder selection) {
        return selection.from("sections s")
                .leftJoin("programs p ON p.id=s.program_id")
                .inOrg("p.org_id");
    }

    private QueryBuilder data() {
        return select("s.*");
    }

    public int getCount(String search, int programId) {
        return getCount(select(count())
                .search(searchParser.parse(search))
                .with("s.program_id", programId));
    }

    public List<Section> get(String search, String sortField, int start, int count, int programId) {
        QueryBuilder query =  select(data()).search(searchParser.parse(search))
                .with("s.program_id", programId)
                .page(sortField, start, count);
        return get(query);
    }

    public Section getById(int id) {
        return getOne(select(data()).with("s.id", id));
    }

    public List<Section> getProgramSections(int programId) {
        return get(select(data()).where("s.program_id=?", programId));
    }

   public Section create(Section section) {
        InsertBuilder cmd = new InsertBuilder().into("sections")
                .value("name", section.getName())
                .value("program_id", section.getProgramId())
                .value("recurrence_id", section.getRecurrenceId())
                .value("day", section.getDayTime().getDayOfWeek().toString())
                .value("time", section.getDayTime().getTimeOfDay());
        section.setId(createAndReturnKey(cmd));
        return section;
    }

    public Section updateSection(Section section) {
        UpdateBuilder cmd = new UpdateBuilder().update("sections")
                .value("id", section.getId())
                .value("name", section.getName())
                .value("program_id", section.getProgramId())
                .value("recurrence_id", section.getRecurrenceId())
                .value("day", section.getDayTime().getDayOfWeek().toString())
                .value("time", section.getDayTime().getTimeOfDay())
                .with("program_id", section.getProgramId())
                .withId(section.getId());

        if(!update(cmd))
            throw new RuntimeException("Failed to update section with id: " + section.getId());

        return section;
    }

    public boolean deleteSection(int id) {
        return delete(new DeleteBuilder().delete("sections").withId(id));
    }

    @Override
    protected Section processRow(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setId(rs.getInt("id"));
        section.setName(rs.getString("name"));
        section.setProgramId(rs.getInt("program_id"));
        section.setRecurrenceId(rs.getInt("recurrence_id"));
        DayTime dt = new DayTime();
        dt.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day")));
        dt.setTimeOfDay(rs.getString("time"));
        section.setDayTime(dt);
        return section;
    }
}
