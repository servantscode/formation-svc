package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Attendance;
import org.servantscode.formation.SessionAttendance;
import org.servantscode.formation.db.AttendanceDB;

import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/program/{programId}/classroom/{classroomId}/attendance")
public class AttendanceSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(Attendance.class);

    private AttendanceDB db;

    public AttendanceSvc() {
        this.db = new AttendanceDB();
    }

    @GET @Produces(APPLICATION_JSON)
    public Attendance getAttendance(@PathParam("programId") int programId,
                                    @PathParam("classroomId") int classroomId) {

        verifyUserAccess("program.attendance.read");
        if (programId <= 0 || classroomId <= 0)
            throw new NotFoundException();

        try {

            Attendance a = db.getClassroomAttendance(classroomId);

            if (a == null || a.getProgramId() != programId)
                throw new NotFoundException();

            return a;
        } catch(Throwable t) {
            LOG.error("Failed to retrieve attendance report", t);
            throw t;
        }
    }

    @PUT @Consumes(APPLICATION_JSON) @Produces(APPLICATION_JSON)
    public Attendance recordClassroomAttendance(@PathParam("programId") int programId,
                                              @PathParam("classroomId") int classroomId,
                                              SessionAttendance attendance) {
        verifyUserAccess("program.attendance.create");
        if (programId <= 0 || classroomId <= 0)
            throw new NotFoundException();

        try {
            db.upsertSessionAttendance(attendance);

            return getAttendance(programId, classroomId);
        } catch(Throwable t) {
            LOG.error("Failed to retrieve attendance report", t);
            throw t;
        }
    }
}
