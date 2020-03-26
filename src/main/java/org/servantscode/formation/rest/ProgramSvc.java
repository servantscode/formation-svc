package org.servantscode.formation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.client.ApiClientFactory;
import org.servantscode.commons.pdf.PdfWriter;
import org.servantscode.commons.pdf.PdfWriter.SpecialColumns;
import org.servantscode.commons.rest.PaginatedResponse;
import org.servantscode.commons.rest.SCServiceBase;
import org.servantscode.formation.Classroom;
import org.servantscode.formation.Program;
import org.servantscode.formation.Student;
import org.servantscode.formation.db.ProgramDB;
import org.servantscode.formation.db.ClassroomDB;
import org.servantscode.formation.db.StudentDB;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static org.servantscode.commons.pdf.PdfWriter.Alignment.CENTER;
import static org.servantscode.commons.pdf.PdfWriter.Alignment.LEFT;

@Path("/program")
public class ProgramSvc extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(ProgramSvc.class);

    private ProgramDB db;
    private ClassroomDB classroomDb;
    private StudentDB studentDb;

    public ProgramSvc() {
        db = new ProgramDB();
        classroomDb = new ClassroomDB();
        studentDb = new StudentDB();
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

    @GET @Path("/{id}/attendanceSheets") @Produces("application/pdf")
    public Response generateAttendanceSheets(@PathParam("id") int id) {
        verifyUserAccess("donation.read");
        try {
            ApiClientFactory.instance().authenticateAsSystem();
            final List<Classroom> classrooms = classroomDb.getProgramClassrooms(id);
            final Map<Integer, List<Student>> classAssignments = studentDb.getProgramClassAssignments(id);

            StreamingOutput stream = output -> {
                createAttendanceSheets(classrooms, classAssignments, output);
            };

            return Response.ok(stream).build();

        } catch(Throwable t) {
            LOG.error("Retrieving annual report failed:", t);
            throw t;
        }

    }

    private void createAttendanceSheets(List<Classroom> classrooms, Map<Integer, List<Student>> classAssignments, OutputStream output) {
        try (PdfWriter writer = new PdfWriter()) {
            for(int i = 0; i< classrooms.size(); i++){
                Classroom classroom = classrooms.get(i);
                createAttendanceSheet(classroom, classAssignments.get(classroom.getId()), i+1, writer);
            }

            writer.writeToStream(output);
        } catch (Throwable t) {
            LOG.error("Failed to create pdf document for annual report", t);
            throw new WebApplicationException("Failed to create annual report pdf", t);
        }
    }

    private void createAttendanceSheet(Classroom s, List<Student> students, int pageNumber, PdfWriter writer) throws IOException {
        if(pageNumber > 1) writer.newPage();

        writer.beginText();
        writer.setFontSize(20);
        writer.addLine(s.getInstructorName());
        writer.addBlankSpace(0.5f);

        writer.setFontSize(12);
        writer.addLine("Class: " + s.getName());
        writer.addLine("Room: " + s.getRoomName());
        writer.addBlankLine();

        writer.startTable(new int[] {20, 160, 160, 80, 120}, new PdfWriter.Alignment[] {CENTER, LEFT, LEFT, LEFT, LEFT});
        writer.addTableHeader("Attd.", "Student", "Contact", "Phone #", "Allergies");
        for(Student student: students) {
            writer.addTableRow(SpecialColumns.CHECKBOX, student.getEnrolleeName(), student.getParentNames().get(0),
                               student.getParentPhones().get(0), join(", ", student.getAllergies()));
        }

        writer.endText();
    }
}
