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
            LOG.info("-- Created program groups table");
            runSql("CREATE TABLE program_groups(id SERIAL PRIMARY KEY, " +
                                               "name TEXT NOT NULL, " +
                                               "complete BOOLEAN DEFAULT FALSE, " +
                                               "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("programs")) {
            LOG.info("-- Created programs table");
            runSql("CREATE TABLE programs(id SERIAL PRIMARY KEY, " +
                                         "name TEXT NOT NULL, " +
                                         "group_id INTEGER REFERENCES program_groups(id) ON DELETE CASCADE, " +
                                         "coordinator_id INTEGER REFERENCES people(id) ON DELETE SET NULL, " +
                                         "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("sections")) {
            LOG.info("-- Created sections table");
            runSql("CREATE TABLE sections(id SERIAL PRIMARY KEY, " +
                                        "name TEXT NOT NULL, " +
                                        "program_id INTEGER REFERENCES programs(id) ON DELETE CASCADE, " +
                                        "instructor_id INTEGER REFERENCES people(id) ON DELETE SET NULL, " +
                                        "room_id INTEGER REFERENCES rooms(id) ON DELETE SET NULL, " +
                                        "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("sacramental_groups")) {
            LOG.info("-- Created sacramental groups table");
            runSql("CREATE TABLE sacramental_groups(id SERIAL PRIMARY KEY, " +
                                                   "name TEXT NOT NULL, " +
                                                   "org_id INTEGER references organizations(id) ON DELETE CASCADE)");
        }

        if(!tableExists("registrations")) {
            LOG.info("-- Created registrations table");
            runSql("CREATE TABLE registrations(enrollee_id INTEGER REFERENCES people(id) ON DELETE CASCADE, " +
                                              "section_id INTEGER REFERENCES sections(id) ON DELETE SET NULL, " +
                                              "grade INTEGER, " +
                                              "sacramental_group_id INTEGER REFERENCES sacramental_groups(id) ON DELETE SET NULL)");
        }
    }
}
