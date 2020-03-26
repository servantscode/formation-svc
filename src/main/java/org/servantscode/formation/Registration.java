package org.servantscode.formation;

import java.util.List;

public class Registration {
    private int id;
    private int enrolleeId;
    private String enrolleeName;
    private int programId;
    private int classroomId;
    private String classroomName;
    private int enrolleeAge;
    private String schoolGrade;
    private int sacramentalGroupId;
    private String sacramentalGroupName;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEnrolleeId() { return enrolleeId; }
    public void setEnrolleeId(int enrolleeId) { this.enrolleeId = enrolleeId; }

    public String getEnrolleeName() { return enrolleeName; }
    public void setEnrolleeName(String enrolleeName) { this.enrolleeName = enrolleeName; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getClassroomId() { return classroomId; }
    public void setClassroomId(int classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public int getEnrolleeAge() { return enrolleeAge; }
    public void setEnrolleeAge(int enrolleeAge) { this.enrolleeAge = enrolleeAge; }

    public String getSchoolGrade() { return schoolGrade; }
    public void setSchoolGrade(String schoolGrade) { this.schoolGrade = schoolGrade; }

    public int getSacramentalGroupId() { return sacramentalGroupId; }
    public void setSacramentalGroupId(int sacramentalGroupId) { this.sacramentalGroupId = sacramentalGroupId; }

    public String getSacramentalGroupName() { return sacramentalGroupName; }
    public void setSacramentalGroupName(String sacramentalGroupName) { this.sacramentalGroupName = sacramentalGroupName; }
}
