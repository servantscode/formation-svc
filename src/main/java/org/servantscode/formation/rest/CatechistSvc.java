package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Catechist;
import org.servantscode.formation.db.CatechistDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program/{programId}/catechist")
public class CatechistSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(CatechistSvc.class);

    private CatechistDB db;

    public CatechistSvc() {
        db = new CatechistDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Catechist> getCatechists(@PathParam("programId") int programId,
                                                      @QueryParam("start") @DefaultValue("0") int start,
                                                      @QueryParam("count") @DefaultValue("10") int count,
                                                      @QueryParam("sort_field") @DefaultValue("enrollee_name") String sortField,
                                                      @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.catechist.list");
        try {
            int totalPeople = db.getCount(search, programId);

            List<Catechist> results = db.get(search, sortField, start, count, programId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving catechists failed:", t);
            throw t;
        }
    }

    @GET @Path("/emails") @Produces(MediaType.APPLICATION_JSON)
    public List<String> getCatechistEmails(@PathParam("programId") int programId,
                                          @QueryParam("search") @DefaultValue("") String search) {

        verifyUserAccess("program.catechist.list");
        try {
            return db.getEmails(search, programId);
        } catch (Throwable t) {
            LOG.error("Retrieving catechist email addresses failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Catechist getCatechist(@PathParam("id") int id) {
        verifyUserAccess("program.catechist.read");
        try {
            return db.getById(id);
        } catch (Throwable t) {
            LOG.error("Retrieving catechist failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Catechist createCatechist(Catechist catechist) {
        verifyUserAccess("program.catechist.create");
        try {
            db.create(catechist);
            LOG.info("Created catechist: " + catechist.getId());
            return catechist;
        } catch (Throwable t) {
            LOG.error("Creating catechist failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Catechist updateCatechist(Catechist catechist) {
        verifyUserAccess("program.catechist.update");
        try {
            db.updateCatechist(catechist);
            LOG.info("Edited catechist: " + catechist.getId());
            return catechist;
        } catch (Throwable t) {
            LOG.error("Updating catechist failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteCatechist(@PathParam("id") int id) {
        verifyUserAccess("program.catechist.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Catechist catechist = db.getById(id);
            if(catechist == null || !db.deleteCatechist(id))
                throw new NotFoundException();
            LOG.info("Deleted catechist: " + catechist.getId());
        } catch (Throwable t) {
            LOG.error("Deleting catechist failed:", t);
            throw t;
        }
    }
}
