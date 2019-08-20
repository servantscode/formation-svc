package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.SacramentalGroup;
import org.servantscode.formation.db.SacramentalGroupDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/sacramental-group")
public class SacramentalGroupSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SacramentalGroupSvc.class);

    private SacramentalGroupDB db;

    public SacramentalGroupSvc() {
        db = new SacramentalGroupDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<SacramentalGroup> getSacramentalGroups(@QueryParam("start") @DefaultValue("0") int start,
                                                                    @QueryParam("count") @DefaultValue("10") int count,
                                                                    @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                                    @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("sacramental-group.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<SacramentalGroup> results = db.getSacramentalGroups(nameSearch, sortField, start, count);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving sacramental groups failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public SacramentalGroup getSacramentalGroup(@PathParam("id") int id) {
        verifyUserAccess("sacramental-group.read");
        try {
            return db.getSacramentalGroup(id);
        } catch (Throwable t) {
            LOG.error("Retrieving sacramental group failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public SacramentalGroup createSacramentalGroup(SacramentalGroup sacramentalGroup) {
        verifyUserAccess("sacramental-group.create");
        try {
            db.create(sacramentalGroup);
            LOG.info("Created sacramental group: " + sacramentalGroup.getName());
            return sacramentalGroup;
        } catch (Throwable t) {
            LOG.error("Creating sacramental group failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public SacramentalGroup updateSacramentalGroup(SacramentalGroup sacramentalGroup) {
        verifyUserAccess("sacramental-group.update");
        try {
            db.updateSacramentalGroup(sacramentalGroup);
            LOG.info("Edited sacramental group: " + sacramentalGroup.getName());
            return sacramentalGroup;
        } catch (Throwable t) {
            LOG.error("Updating sacramental group failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteSacramentalGroup(@PathParam("id") int id) {
        verifyUserAccess("sacramental-group.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            SacramentalGroup sacramentalGroup = db.getSacramentalGroup(id);
            if(sacramentalGroup == null || !db.deleteSacramentalGroup(id))
                throw new NotFoundException();
            LOG.info("Deleted sacramental group: " + sacramentalGroup.getName());
        } catch (Throwable t) {
            LOG.error("Deleting sacramental group failed:", t);
            throw t;
        }
    }
}
