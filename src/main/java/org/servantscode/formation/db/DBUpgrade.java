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
            LOG.info("-- Creating sections table");
            runSql("CREATE TABLE sections(id SERIAL PRIMARY KEY, " +
                                        "name TEXT NOT NULL, " +
                                        "program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE, " +
                                        "instructor_id INTEGER REFERENCES people(id) ON DELETE SET NULL, " +
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
                                              "grade TEXT, " +
                                              "sacramental_group_id INTEGER REFERENCES sacramental_groups(id) ON DELETE SET NULL)");
        }

        if(!tableExists("program_sessions")) {
            LOG.info("-- Creating program sessions table");
            runSql("CREATE TABLE program_sessions(id SERIAL PRIMARY KEY, " +
                                         "program_id INTEGER NOT NULL REFERENCES programs(id) ON DELETE CASCADE, " +
                                         "event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE)");
        }

        if(!tableExists("attendance")) {
            LOG.info("-- Creating attendance table.");
            runSql("CREATE TABLE attendance (enrollee_id INTEGER NOT NULL REFERENCES people(id) ON DELETE CASCADE, " +
                                            "classroom_id INTEGER NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE, " +
                                            "session_id INTEGER NOT NULL REFERENCES program_sessions(id) ON DELETE CASCADE, " +
                                            "attendance BOOLEAN NOT NULL," +
                                            "PRIMARY KEY (enrollee_id, classroom_id, session_id))");
        }

        // 8/22/19
        if(!columnExists("registrations", "id")) {
            runSql("ALTER TABLE registrations ADD COLUMN id SERIAL PRIMARY KEY");
        }

        if(!columnExists("registrations", "program_id")) {
            runSql("ALTER TABLE registrations ADD COLUMN program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE");
        }

        if(!columnExists("registrations", "school_grade")) {
            runSql("ALTER TABLE registrations ADD COLUMN school_grade TEXT");
            runSql("UPDATE registrations SET school_grade=grade");
            runSql("ALTER TABLE registrations DROP COLUMN grade");
        }

        if(!columnExists("attendance", "classroom_id")) {
            runSql("ALTER TABLE attendance RENAME COLUMN section_id TO classroom_id");
            runSql("ALTER TABLE attendance RENAME CONSTRAINT attendance_section_id_fkey TO attendance_classroom_id_fkey");
        }

        if(!columnExists("registrations", "classroom_id")) {
            runSql("ALTER TABLE registrations RENAME COLUMN section_id TO classroom_id");
            runSql("ALTER TABLE registrations RENAME CONSTRAINT registrations_section_id_fkey TO registrations_classroom_id_fkey");
        }

        if(!tableExists("classrooms")) {
            runSql("ALTER TABLE sections RENAME TO classrooms");
            runSql("ALTER INDEX sections_pkey RENAME TO classrooms_pkey");
        }
    }
}
