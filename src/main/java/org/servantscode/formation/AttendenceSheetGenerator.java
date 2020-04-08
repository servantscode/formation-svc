package org.servantscode.formation;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.servantscode.commons.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static org.servantscode.commons.pdf.PdfWriter.Alignment.CENTER;
import static org.servantscode.commons.pdf.PdfWriter.Alignment.LEFT;

public class AttendenceSheetGenerator {
    private static final Logger LOG = LogManager.getLogger(AttendenceSheetGenerator.class);

    public void createAttendanceSheets(List<Classroom> classrooms, Map<Integer, List<Student>> classAssignments, OutputStream output) throws IOException {
        try (PdfWriter writer = new PdfWriter()) {
            for(int i = 0; i< classrooms.size(); i++){
                Classroom classroom = classrooms.get(i);
                createAttendanceSheet(classroom, classAssignments.get(classroom.getId()), i+1, writer);
            }

            writer.writeToStream(output);
        }
    }

    private void createAttendanceSheet(Classroom s, List<Student> students, int pageNumber, PdfWriter writer) throws IOException {
        if(pageNumber > 1) writer.newPage();

        writer.beginText();
        writer.setFontSize(20);
        writer.addLine(s.getInstructorName());
        writer.addBlankSpace(0.5f);

        writer.setFontSize(12);
        if(s.getAdditionalInstructorNames().size() > 0) {
            writer.addLine("Assistants: " + join(", ", s.getAdditionalInstructorNames()));
            writer.addBlankLine();
        }

        writer.addLine("Class: " + s.getName());
        writer.addLine("Room: " + s.getRoomName());
        writer.addBlankLine();

        writer.startTable(new int[] {20, 160, 160, 80, 120}, new PdfWriter.Alignment[] {CENTER, LEFT, LEFT, LEFT, LEFT});
        writer.addTableHeader("Attd.", "Student", "Contact", "Phone #", "Allergies");
        for(Student student: students) {
            writer.addTableRow(PdfWriter.SpecialColumns.CHECKBOX, student.getEnrolleeName(),
                    student.getParentNames().isEmpty()? "": student.getParentNames().get(0),
                    student.getParentPhones().isEmpty()? "": student.getParentPhones().get(0),
                    join(", ", student.getAllergies()));
        }

        writer.endText();
    }
}
