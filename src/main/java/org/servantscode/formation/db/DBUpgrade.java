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
                    "section_id INTEGER NOT NULL REFERENCES sections(id) ON DELETE CASCADE, " +
                    "event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE)");
        }

        if(!tableExists("classrooms")) {
            LOG.info("-- Creating classrooms table");
            runSql("CREATE TABLE classrooms(id SERIAL PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE, " +
                    "section_id INTEGER NOT NULL REFERENCES sections(id) ON DELETE CASCADE, " +
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
        }

        if(!tableExists("milestones")) {
            LOG.info("-- Creating milestones table.");
            runSql("CREATE TABLE milestones (id SERIAL PRIMARY KEY, classroom_id INTEGER NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE, name TEXT)");
        }

        if(!tableExists("milestone_completions")) {
            LOG.info("-- Creating milestone_completions table.");
            runSql("CREATE TABLE milestone_completions (enrollee_id INTEGER NOT NULL REFERENCES people(id) ON DELETE CASCADE, " +
                    "classroom_id INTEGER NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE, " +
                    "milestone_id INTEGER NOT NULL REFERENCES milestones(id) ON DELETE CASCADE, " +
                    "complete BOOLEAN NOT NULL, " +
                    "PRIMARY KEY (enrollee_id, classroom_id, milestone_id))");
        }
    }
}
