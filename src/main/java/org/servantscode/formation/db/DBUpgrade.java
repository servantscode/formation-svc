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

        if(!tableExists("program_sessions")) {
            LOG.info("-- Creating program sessions table");
            runSql("CREATE TABLE program_sessions(id SERIAL PRIMARY KEY, " +
                    "program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, " +
                    "event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE)");
        }

        if(!tableExists("classrooms")) {
            LOG.info("-- Creating classrooms table");

            if(tableExists("sections"))
                runSql("ALTER TABLE sections RENAME TO classrooms");
            else
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

        // 2020-03-28
        if(!columnExists("attendance", "classroom_id") && columnExists("attendance", "section_id")) {
            runSql("ALTER TABLE attendance RENAME COLUMN section_id TO classroom_id");
            runSql("ALTER TABLE attendance RENAME CONSTRAINT attendance_section_id_fkey TO attendance_classroom_id_fkey");
        }

        if(!columnExists("registrations", "classroom_id")) {
            runSql("ALTER TABLE registrations RENAME COLUMN section_id TO classroom_id");
            runSql("ALTER TABLE registrations RENAME CONSTRAINT registrations_section_id_fkey TO registrations_classroom_id_fkey");
        }

        if(indexExists("classrooms", "sections_pkey"))
            runSql("ALTER INDEX sections_pkey RENAME TO classrooms_pkey");

        if(indexExists("classrooms", "sections_org_id_fkey"))
            runSql("ALTER TABLE classrooms RENAME CONSTRAINT sections_org_id_fkey TO classrooms_org_id_fkey");

        if(indexExists("classrooms", "sections_program_id_fkey"))
            runSql("ALTER TABLE classrooms RENAME CONSTRAINT sections_program_id_fkey TO classrooms_program_id_fkey");

        if(indexExists("classrooms", "sections_room_id_fkey"))
            runSql("ALTER TABLE classrooms RENAME CONSTRAINT sections_room_id_fkey TO classrooms_sections_room_id_fkey");
    }
}
