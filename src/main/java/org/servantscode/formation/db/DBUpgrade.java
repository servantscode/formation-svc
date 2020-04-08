package org.servantscode.formation.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.db.AbstractDBUpgrade;

import java.sql.SQLException;

public class DBUpgrade extends AbstractDBUpgrade {
    private static final Logger LOG = LogManager.getLogger(DBUpgrade.class);

    @Override
    public void doUpgrade() throws SQLException {
        LOG.info("Verifying database structures.");

        if(!tableExists("program_groups")) {
            LOG.info("-- Creating program groups table");
            runSql("CREATE TABLE program_groups(id SERIAL PRIMARY KEY, " +
                                               "name TEXT NOT NULL, " +
                                               "complete BOOLEAN DEFAULT FALSE, " +
                                               "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("programs")) {
            LOG.info("-- Creating programs table");
            runSql("CREATE TABLE programs(id SERIAL PRIMARY KEY, " +
                                         "name TEXT NOT NULL, " +
                                         "group_id INTEGER REFERENCES program_groups(id) ON DELETE CASCADE, " +
                                         "coordinator_id INTEGER REFERENCES people(id) ON DELETE SET NULL, " +
                                         "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("sections")) {
            if(columnExists("sections", "instructor_id"))
                throw new IllegalStateException("Invalid sections table found");

            LOG.info("-- Creating sections table");
            runSql("CREATE TABLE sections (id SERIAL PRIMARY KEY, " +
                   "name TEXT NOT NULL, " +
                   "program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, " +
                   "recurrence_id INTEGER REFERENCES recurrences(id) ON DELETE SET NULL, " +
                   "day TEXT, " +
                   "time TEXT)");
        }

        //CREATE TABLE sections (id SERIAL PRIMARY KEY, name TEXT NOT NULL, program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, recurrence_id INTEGER REFERENCES recurrences(id) ON DELETE SET NULL, day TEXT, time TEXT)

        if(!tableExists("program_sessions")) {
            LOG.info("-- Creating program sessions table");
            runSql("CREATE TABLE program_sessions(id SERIAL PRIMARY KEY, " +
                    "program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, " +
                    "event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE)");
        }

        if(!tableExists("classrooms")) {
            LOG.info("-- Creating classrooms table");
            runSql("CREATE TABLE classrooms(id SERIAL PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE, " +
                    "room_id INTEGER REFERENCES rooms(id) ON DELETE SET NULL, " +
                    "complete BOOLEAN DEFAULT false, " +
                    "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("sacramental_groups")) {
            LOG.info("-- Creating sacramental groups table");
            runSql("CREATE TABLE sacramental_groups(id SERIAL PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("registrations")) {
            LOG.info("-- Creating registrations table");
            runSql("CREATE TABLE registrations(id SERIAL PRIMARY KEY, " +
                    "enrollee_id INTEGER REFERENCES people(id) ON DELETE CASCADE, " +
                    "program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE, " +
                    "classroom_id INTEGER REFERENCES classrooms(id) ON DELETE SET NULL, " +
                    "school_grade TEXT, " +
                    "sacramental_group_id INTEGER REFERENCES sacramental_groups(id) ON DELETE SET NULL)");
        }

        if(!tableExists("attendance")) {
            LOG.info("-- Creating attendance table.");
            runSql("CREATE TABLE attendance (enrollee_id INTEGER NOT NULL REFERENCES people(id) ON DELETE CASCADE, " +
                    "classroom_id INTEGER NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE, " +
                    "session_id INTEGER NOT NULL REFERENCES program_sessions(id) ON DELETE CASCADE, " +
                    "attendance BOOLEAN NOT NULL," +
                    "PRIMARY KEY (enrollee_id, classroom_id, session_id))");
        }

        if(!tableExists("catechists")) {
            LOG.info("-- Creating catechists table.");
            runSql("CREATE TABLE catechists (id INTEGER NOT NULL REFERENCES people(id) ON DELETE CASCADE, " +
                   "program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, " +
                   "classroom_id INTEGER REFERENCES classrooms(id) ON DELETE SET NULL, " +
                   "is_primary BOOLEAN NOT NULL DEFAULT false)");

            if(columnExists("classrooms", "instructor_id")) {
                runSql("INSERT INTO catechists (SELECT instructor_id AS id, program_id, id AS classroom_id, true AS is_primary FROM classrooms)");
                runSql("ALTER TABLE classrooms DROP COLUMN instructor_id");
            }
        }

        if(!columnExists("program_sessions", "section_id")) {
            runSql("INSERT INTO sections (name, program_id, recurrence_id, day, time) " +
                   "SELECT DISTINCT " +
                   "concat(trim(to_char(e.start_time, 'day')), ' ', extract(hour from e.start_time), ':', LPAD(extract(minute from e.start_time)::TEXT, 2, '0')), " +
                   "s.program_id, e.recurring_meeting_id, UPPER(trim(to_char(e.start_time, 'day'))), " +
                   "concat(extract(hour from e.start_time), ':', LPAD(extract(minute from e.start_time)::TEXT, 2, '0')) " +
                   "FROM program_sessions s " +
                   "LEFT JOIN events e ON s.event_id=e.id");
            runSql("ALTER TABLE program_sessions ADD COLUMN section_id INTEGER");
            runSql("UPDATE program_sessions ps SET section_id=s.id FROM sections s left join events e on s.recurrence_id=e.recurring_meeting_id where ps.event_id=e.id");
            runSql("ALTER TABLE program_sessions ADD CONSTRAINT program_sessions_section_id_fkey FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE");
        }

        if(!columnExists("classrooms", "section_id")) {
            runSql("ALTER TABLE classrooms ADD COLUMN section_id INTEGER");
            runSql("UPDATE classrooms c SET section_id=s.id FROM sections s where c.program_id=s.program_id");
            runSql("ALTER TABLE classrooms ADD CONSTRAINT program_sessions_section_id_fkey FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE");
        }
    }
}
