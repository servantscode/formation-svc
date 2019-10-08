package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Session;
import org.servantscode.formation.SessionSeries;
import org.servantscode.formation.db.SessionDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program/{programId}/session")
public class SessionSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SessionSvc.class);

    private SessionDB db;

    public SessionSvc() {
        db = new SessionDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Session> getSessions(@PathParam("programId") int programId,
                                                  @QueryParam("start") @DefaultValue("0") int start,
                                                  @QueryParam("count") @DefaultValue("10") int count,
                                                  @QueryParam("sort_field") @DefaultValue("start_time") String sortField,
                                                  @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.session.list");
        try {
            int totalPeople = db.getCount(search, programId);

            List<Session> results = db.get(search, sortField, start, count, programId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving sessions failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Session getSession(@PathParam("programId") int programId,
                              @PathParam("id") int id) {

        verifyUserAccess("program.session.read");
        try {
            Session session = db.getById(id);
            if(session.getProgramId() != programId)
                throw new NotFoundException();
            return session;
        } catch (Throwable t) {
            LOG.error("Retrieving session failed:", t);
            throw t;
        }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public void createSession(@PathParam("programId") int programId,
                              SessionSeries series) {
        verifyUserAccess("program.session.create");
        try {
            if(series.getProgramId() != programId)
                throw new BadRequestException();

            db.createSeries(series);
            LOG.info("Created session series.");
        } catch (Throwable t) {
            LOG.error("Creating session failed:", t);
            throw t;
        }
    }

//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
//    public Session updateSession(@PathParam("programId") int programId,
//                                 Session session) {
//        verifyUserAccess("program.session.update");
//
//        if(session.getProgramId() != programId)
//            throw new BadRequestException();
//
//        try {
//            db.updateSession(session);
//            LOG.info("Edited session: " + session.getName());
//            return session;
//        } catch (Throwable t) {
//            LOG.error("Updating session failed:", t);
//            throw t;
//        }
//    }

    @DELETE @Path("/{id}")
    public void deleteSession(@PathParam("programId") int programId,
                              @PathParam("id") int id) {
        verifyUserAccess("program.session.delete");
        if(id <= 0)
            throw new NotFoundException();

        try {
            Session session = db.getById(id);
            if(session == null || session.getProgramId() != programId || !db.delete(id))
                throw new NotFoundException();
            LOG.info("Deleted session: " + session.getStartTime());
        } catch (Throwable t) {
            LOG.error("Deleting session failed:", t);
            throw t;
        }
    }
}
