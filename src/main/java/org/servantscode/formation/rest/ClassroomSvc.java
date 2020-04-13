package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Catechist;
import org.servantscode.formation.Classroom;
import org.servantscode.formation.db.CatechistDB;
import org.servantscode.formation.db.ClassroomDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.servantscode.commons.StringUtils.isSet;

@Path("/program/{programId}{section: (/section/\\d+)?}/classroom")
public class ClassroomSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ClassroomSvc.class);

    private ClassroomDB db;
    private CatechistDB catechistDb;

    public ClassroomSvc() {
        db = new ClassroomDB();
        catechistDb = new CatechistDB();
    }

    @GET @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResponse<Classroom> getClassrooms(@PathParam("programId") int programId,
                                                    @PathParam("section") String optionalSection,
                                                    @QueryParam("start") @DefaultValue("0") int start,
                                                    @QueryParam("count") @DefaultValue("10") int count,
                                                    @QueryParam("sort_field") @DefaultValue("name") String sortField,
                                                    @QueryParam("search") @DefaultValue("") String search) {

        int sectionId = parseSectionId(optionalSection);
        verifyUserAccess("program.classroom.list");
        try {
            int totalPeople;
            List<Classroom> results;

            if(sectionId > 0) {
                totalPeople = db.getSectionCount(search, sectionId);
                results = db.getSectionClassrooms(search, sortField, start, count, sectionId);
            } else {
                totalPeople = db.getProgramCount(search, programId);
                results = db.getProgramClassrooms(search, sortField, start, count, programId);
            }

            return new PaginatedResponse<>(start, results.size(), totalPeople, results);
        } catch (Throwable t) {
            LOG.error("Retrieving classrooms failed:", t);
            throw t;
        }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Classroom getClassroom(@PathParam("programId") int programId,
                                  @PathParam("section") String optionalSection,
                                  @PathParam("id") int id) {

        int sectionId = parseSectionId(optionalSection);

        verifyUserAccess("program.classroom.read");
        try {
            Classroom classroom = db.getById(id);
            if(classroom.getProgramId() != programId ||
                    (sectionId > 0 && classroom.getSectionId() != sectionId))
                throw new NotFoundException();
            return classroom;
        } catch (Throwable t) {
            LOG.error("Retrieving classroom failed:", t);
            throw t;
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Classroom createClassroom(@PathParam("programId") int programId,
                                     @PathParam("section") String requiredSection,
                                     Classroom classroom) {

        int sectionId = parseSectionId(requiredSection);
        verifyUserAccess("program.classroom.create");
        try {
            if(classroom.getProgramId() != programId || classroom.getSectionId() != sectionId)
                throw new BadRequestException();
            db.create(classroom);
            if(classroom.getInstructorId() > 0)
                catechistDb.create(new Catechist(classroom.getInstructorId(), classroom.getProgramId(), classroom.getId()));
            LOG.info("Created classroom: " + classroom.getName());
            return classroom;
        } catch (Throwable t) {
            LOG.error("Creating classroom failed:", t);
            throw t;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Classroom updateClassroom(@PathParam("programId") int programId,
                                     @PathParam("section") String requiredSection,
                                     Classroom classroom) {

        int sectionId = parseSectionId(requiredSection);
        verifyUserAccess("program.classroom.update");

        if(classroom.getProgramId() != programId || classroom.getSectionId() != sectionId)
            throw new BadRequestException();

        try {
            db.updateClassroom(classroom);
            if(classroom.getInstructorId() > 0)
                catechistDb.updateClassroomCatechist(new Catechist(classroom.getInstructorId(), classroom.getProgramId(), classroom.getId()));
            else
                catechistDb.removeClassroomCatechist(classroom.getProgramId(), classroom.getId());
            LOG.info("Edited classroom: " + classroom.getName());
            return classroom;
        } catch (Throwable t) {
            LOG.error("Updating classroom failed:", t);
            throw t;
        }
    }

    @DELETE @Path("/{id}")
    public void deleteClassroom(@PathParam("programId") int programId,
                                @PathParam("section") String requiredSection,
                                @PathParam("id") int id) {

        int sectionId = parseSectionId(requiredSection);
        verifyUserAccess("program.classroom.delete");
        if(id <= 0)
            throw new NotFoundException();
        try {
            Classroom classroom = db.getById(id);
            if(classroom == null || classroom.getProgramId() != programId || classroom.getSectionId() != sectionId || !db.deleteClassroom(id))
                throw new NotFoundException();
            LOG.info("Deleted classroom: " + classroom.getName());
        } catch (Throwable t) {
            LOG.error("Deleting classroom failed:", t);
            throw t;
        }
    }

    // ----- Private -----
    private int parseSectionId(@PathParam("section") String optionalSection) {
        int sectionId = isSet(optionalSection)? parseInt(optionalSection.split("\\/")[2]) : 0;
        LOG.debug("Found sectionId: " + sectionId);
        return sectionId;
    }

}
