package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Program;
import org.servantscode.formation.db.ProgramDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program")
public class ProgramSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ProgramSvc.class);

    private ProgramDB db;

    public ProgramSvc() {
        db = new ProgramDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Program> getPrograms(@QueryParam("start") @DefaultValue("0") int start,
                                                  @QueryParam("count") @DefaultValue("10") int count,
                                                  @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                  @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("program.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<Program> results = db.getPrograms(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving programs failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Program getProgram(@PathParam("id") int id) {
        verifyUserAccess("program.read");
        try {
            return db.getProgram(id);
        } catch (Throwable t) {
            LOG.error("Retrieving program failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Program createProgram(Program program) {
        verifyUserAccess("program.create");
        try {
            db.create(program);
            LOG.info("Created program: " + program.getName());
            return program;
        } catch (Throwable t) {
            LOG.error("Creating program failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Program updateProgram(Program program) {
        verifyUserAccess("program.update");
        try {
            db.updateProgram(program);
            LOG.info("Edited program: " + program.getName());
            return program;
        } catch (Throwable t) {
            LOG.error("Updating program failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteProgram(@PathParam("id") int id) {
        verifyUserAccess("program.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Program program = db.getProgram(id);
            if(program == null || !db.deleteProgram(id))
                throw new NotFoundException();
            LOG.info("Deleted program: " + program.getName());
        } catch (Throwable t) {
            LOG.error("Deleting program failed:", t);
            throw t;
        }
    }
}
