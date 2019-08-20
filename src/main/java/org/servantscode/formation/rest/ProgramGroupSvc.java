package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.ProgramGroup;
import org.servantscode.formation.db.ProgramGroupDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program/group")
public class ProgramGroupSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ProgramGroupSvc.class);

    private ProgramGroupDB db;

    public ProgramGroupSvc() {
        db = new ProgramGroupDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<ProgramGroup> getProgramGroups(@QueryParam("start") @DefaultValue("0") int start,
                                                            @QueryParam("count") @DefaultValue("10") int count,
                                                            @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                            @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("program.group.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<ProgramGroup> results = db.getProgramGroups(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving program groups failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public ProgramGroup getProgramGroup(@PathParam("id") int id) {
        verifyUserAccess("program.group.read");
        try {
            return db.getProgramGroup(id);
        } catch (Throwable t) {
            LOG.error("Retrieving program group failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public ProgramGroup createProgramGroup(ProgramGroup programGroup) {
        verifyUserAccess("program.group.create");
        try {
            db.create(programGroup);
            LOG.info("Created program group: " + programGroup.getName());
            return programGroup;
        } catch (Throwable t) {
            LOG.error("Creating program group failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public ProgramGroup updateProgramGroup(ProgramGroup programGroup) {
        verifyUserAccess("program.group.update");
        try {
            db.updateProgramGroup(programGroup);
            LOG.info("Edited program group: " + programGroup.getName());
            return programGroup;
        } catch (Throwable t) {
            LOG.error("Updating program group failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteProgramGroup(@PathParam("id") int id) {
        verifyUserAccess("program.group.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            ProgramGroup programGroup = db.getProgramGroup(id);
            if(programGroup == null || !db.deleteProgramGroup(id))
                throw new NotFoundException();
            LOG.info("Deleted program group: " + programGroup.getName());
        } catch (Throwable t) {
            LOG.error("Deleting program group failed:", t);
            throw t;
        }
    }
}
