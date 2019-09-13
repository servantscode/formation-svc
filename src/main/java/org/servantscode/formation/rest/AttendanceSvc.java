package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Attendance;
import org.servantscode.formation.SessionAttendance;
import org.servantscode.formation.db.AttendanceDB;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/program/{programId}/section/{sectionId}/attendance")
public class AttendanceSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(Attendance.class);

    private AttendanceDB db;

    public AttendanceSvc() {
        this.db = new AttendanceDB();
    }

    @GET @Produces(APPLICATION_JSON)
    public Attendance getAttendance(@PathParam("programId") int programId,
                                    @PathParam("sectionId") int sectionId) {

        verifyUserAccess("program.attendance.read");
        if (programId <= 0 || sectionId <= 0)
            throw new NotFoundException();

        try {

            Attendance a = db.getSectionAttendance(sectionId);

            if (a == null || a.getProgramId() != programId)
                throw new NotFoundException();

            return a;
        } catch(Throwable t) {
            LOG.error("Failed to retrieve attendance report", t);
            throw t;
        }
    }

    @PUT @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public Attendance recordSectionAttendance(@PathParam("programId") int programId,
                                              @PathParam("sectionId") int sectionId,
                                              SessionAttendance attendance) {
        verifyUserAccess("program.attendance.create");
        if (programId <= 0 || sectionId <= 0)
            throw new NotFoundException();

        try {
            db.upsertSessionAttendance(attendance);

            return getAttendance(programId, sectionId);
        } catch(Throwable t) {
            LOG.error("Failed to retrieve attendance report", t);
            throw t;
        }
    }
}
