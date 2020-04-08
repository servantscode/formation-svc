package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.AttendenceSheetGenerator;
import org.servantscode.formation.Classroom;
import org.servantscode.formation.Section;
import org.servantscode.formation.Student;
import org.servantscode.formation.db.ClassroomDB;
import org.servantscode.formation.db.SectionDB;
import org.servantscode.formation.db.SessionDB;
import org.servantscode.formation.db.StudentDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;
import java.util.Map;

@Path("/program/{programId}/section")
public class SectionSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SectionSvc.class);

    private SectionDB db;
    private SessionDB sessionDb;
    private ClassroomDB classroomDb;
    private StudentDB studentDb;

    public SectionSvc() {
        db = new SectionDB();
        sessionDb = new SessionDB();
        classroomDb = new ClassroomDB();
        studentDb = new StudentDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Section> getSections(@PathParam("programId") int programId,
                                                  @QueryParam("start") @DefaultValue("0") int start,
                                                  @QueryParam("count") @DefaultValue("10") int count,
                                                  @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                  @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.section.list");
        try {
            int totalPeople = db.getCount(search, programId);

            List<Section> results = db.get(search, sortField, start, count, programId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving sections failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Section getSection(@PathParam("programId") int programId,
                              @PathParam("id") int id) {

        verifyUserAccess("program.section.read");
        try {
            Section section = db.getById(id);
            if(section.getProgramId() != programId)
                throw new NotFoundException();
            return section;
        } catch (Throwable t) {
            LOG.error("Retrieving section failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}/attendanceSheets") @Produces("application/pdf")
    public Response generateAttendanceSheets(@PathParam("id") int id) {
        verifyUserAccess("donation.read");
        try {
            final List<Classroom> classrooms = classroomDb.getSectionClassrooms(id);
            final Map<Integer, List<Student>> classAssignments = studentDb.getSectionClassAssignments(id);

            StreamingOutput stream = output -> {
                try {
                    new AttendenceSheetGenerator().createAttendanceSheets(classrooms, classAssignments, output);
                } catch (Throwable t) {
                    LOG.error("Failed to create attendance sheet document.", t);
                    throw new WebApplicationException("Failed to create attendance sheets", t);
                }
            };

            return Response.ok(stream).build();

        } catch(Throwable t) {
            LOG.error("Retrieving annual report failed:", t);
            throw t;
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public void createSection(@PathParam("programId") int programId,
                              Section section) {
        verifyUserAccess("program.section.create");
        try {
            if(section.getProgramId() != programId)
                throw new BadRequestException();

            db.create(section);

            if(section.getRecurrenceId() > 0)
                sessionDb.linkSeries(section);

            LOG.info("Created section series.");
        } catch (Throwable t) {
            LOG.error("Creating section failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Section updateSection(@PathParam("programId") int programId,
                                 Section section) {
        verifyUserAccess("program.section.update");

        if(section.getProgramId() != programId)
            throw new BadRequestException();

        Section dbSection = db.getById(section.getId());

        try {
            db.updateSection(section);

            if(dbSection.getRecurrenceId() != section.getRecurrenceId()) {
                if (dbSection.getRecurrenceId() == 0) {
                    sessionDb.linkSeries(section);
                } else if (section.getRecurrenceId() == 0) {
                    sessionDb.deleteSeries(dbSection.getRecurrenceId());
                } else {
                    sessionDb.deleteSeries(dbSection.getRecurrenceId());
                    sessionDb.linkSeries(section);
                }
            }

            LOG.info("Edited section: " + section.getName());
            return section;
        } catch (Throwable t) {
            LOG.error("Updating section failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteSection(@PathParam("programId") int programId,
                              @PathParam("id") int id) {
        verifyUserAccess("program.section.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Section section = db.getById(id);
            if(section == null || section.getProgramId() != programId || !db.deleteSection(id))
                throw new NotFoundException();
            LOG.info("Deleted section: " + section.getName());
        } catch (Throwable t) {
            LOG.error("Deleting section failed:", t);
            throw t;
        }
    }
}
