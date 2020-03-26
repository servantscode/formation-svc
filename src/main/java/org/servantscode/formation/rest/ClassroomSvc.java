package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Classroom;
import org.servantscode.formation.db.ClassroomDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program/{programId}/classroom")
public class ClassroomSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ClassroomSvc.class);

    private ClassroomDB db;

    public ClassroomSvc() {
        db = new ClassroomDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Classroom> getSections(@PathParam("programId") int programId,
                                                    @QueryParam("start") @DefaultValue("0") int start,
                                                    @QueryParam("count") @DefaultValue("10") int count,
                                                    @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                    @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.section.list");
        try {
            int totalPeople = db.getCount(search, programId);

            List<Classroom> results = db.get(search, sortField, start, count, programId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving sections failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Classroom getSection(@PathParam("programId") int programId,
                                @PathParam("id") int id) {
        verifyUserAccess("program.section.read");
        try {
            Classroom classroom = db.getById(id);
            if(classroom.getProgramId() != programId)
                throw new NotFoundException();
            return classroom;
        } catch (Throwable t) {
            LOG.error("Retrieving section failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Classroom createSection(@PathParam("programId") int programId,
                                   Classroom classroom) {
        verifyUserAccess("program.classroom.create");
        try {
            if(classroom.getProgramId() != programId)
                throw new BadRequestException();
            db.create(classroom);
            LOG.info("Created classroom: " + classroom.getName());
            return classroom;
        } catch (Throwable t) {
            LOG.error("Creating classroom failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Classroom updateSection(@PathParam("programId") int programId,
                                   Classroom classroom) {
        verifyUserAccess("program.classroom.update");

        if(classroom.getProgramId() != programId)
            throw new BadRequestException();

        try {
            db.updateClassroom(classroom);
            LOG.info("Edited classroom: " + classroom.getName());
            return classroom;
        } catch (Throwable t) {
            LOG.error("Updating classroom failed:", t);
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
            Classroom classroom = db.getById(id);
            if(classroom == null || classroom.getProgramId() != programId || !db.deleteClassroom(id))
                throw new NotFoundException();
            LOG.info("Deleted classroom: " + classroom.getName());
        } catch (Throwable t) {
            LOG.error("Deleting section failed:", t);
            throw t;
        }
    }
}
