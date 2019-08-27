package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Registration;
import org.servantscode.formation.db.RegistrationDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/program/{programId}/registration")
public class RegistrationSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(RegistrationSvc.class);

    private RegistrationDB db;

    public RegistrationSvc() {
        db = new RegistrationDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Registration> getRegistrations(@PathParam("programId") int programId,
                                                            @QueryParam("start") @DefaultValue("0") int start,
                                                            @QueryParam("count") @DefaultValue("10") int count,
                                                            @QueryParam("sort_field") @DefaultValue("enrollee_name") String sortField,
                                                            @QueryParam("search") @DefaultValue("") String nameSearch) {

        verifyUserAccess("registration.list");
        try {
            int totalPeople = db.getCount(nameSearch);

            List<Registration> results = db.getRegistrations(nameSearch, sortField, start, count, programId);

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving registrations failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Registration getRegistration(@PathParam("id") int id) {
        verifyUserAccess("registration.read");
        try {
            return db.getRegistration(id);
        } catch (Throwable t) {
            LOG.error("Retrieving registration failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Registration createRegistration(Registration registration) {
        verifyUserAccess("registration.create");
        try {
            db.create(registration);
            LOG.info("Created registration: " + registration.getEnrolleeId());
            return registration;
        } catch (Throwable t) {
            LOG.error("Creating registration failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Registration updateRegistration(Registration registration) {
        verifyUserAccess("registration.update");
        try {
            db.updateRegistration(registration);
            LOG.info("Edited registration: " + registration.getEnrolleeId());
            return registration;
        } catch (Throwable t) {
            LOG.error("Updating registration failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteRegistration(@PathParam("id") int id) {
        verifyUserAccess("registration.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Registration registration = db.getRegistration(id);
            if(registration == null || !db.deleteRegistration(registration))
                throw new NotFoundException();
            LOG.info("Deleted registration: " + registration.getEnrolleeId());
        } catch (Throwable t) {
            LOG.error("Deleting registration failed:", t);
            throw t;
        }
    }
}
