package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.AttendenceSheetGenerator;
import org.servantscode.formation.Classroom;
import org.servantscode.formation.Milestone;
import org.servantscode.formation.Student;
import org.servantscode.formation.db.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;
import java.util.Map;

@Path("/program/{classroomId}/classroom/{classroomId}/milestone")
public class MilestoneSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(MilestoneSvc.class);

    private MilestoneDB db;

    public MilestoneSvc() {
        db = new MilestoneDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Milestone> getMilestones(@PathParam("classroomId") int classroomId,
                                                  @QueryParam("start") @DefaultValue("0") int start,
                                                  @QueryParam("count") @DefaultValue("10") int count,
                                                  @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                  @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.milestone.list");
        try {
            int totalPeople = db.getCount(search, classroomId);

            List<Milestone> results = db.get(search, sortField, start, count, classroomId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving milestones failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Milestone getMilestone(@PathParam("classroomId") int classroomId,
                                  @PathParam("id") int id) {

        verifyUserAccess("program.milestone.read");
        try {
            Milestone milestone = db.getById(id);
            if(milestone.getClassroomId() != classroomId)
                throw new NotFoundException();
            return milestone;
        } catch (Throwable t) {
            LOG.error("Retrieving milestone failed:", t);
            throw t;
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public void createMilestone(@PathParam("classroomId") int classroomId,
                              Milestone milestone) {
        verifyUserAccess("program.milestone.create");
        try {
            if(milestone.getClassroomId() != classroomId)
                throw new BadRequestException();

            db.create(milestone);

            LOG.info("Created milestone.");
        } catch (Throwable t) {
            LOG.error("Creating milestone failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Milestone updateMilestone(@PathParam("classroomId") int classroomId,
                                 Milestone milestone) {
        verifyUserAccess("program.milestone.update");

        if(milestone.getClassroomId() != classroomId)
            throw new BadRequestException();

        Milestone dbMilestone = db.getById(milestone.getId());

        try {
            db.update(milestone);

            LOG.info("Edited milestone: " + milestone.getName());
            return milestone;
        } catch (Throwable t) {
            LOG.error("Updating milestone failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteMilestone(@PathParam("classroomId") int classroomId,
                              @PathParam("id") int id) {
        verifyUserAccess("program.milestone.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Milestone milestone = db.getById(id);
            if(milestone == null || milestone.getClassroomId() != classroomId || !db.delete(id))
                throw new NotFoundException();
            LOG.info("Deleted milestone: " + milestone.getName());
        } catch (Throwable t) {
            LOG.error("Deleting milestone failed:", t);
            throw t;
        }
    }
}
