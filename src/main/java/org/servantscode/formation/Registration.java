package org.servantscode.formation;

public class Registration {
    private int enrolleeId;
    private String enrolleeName;
    private int classId;
    private int className;
    private int enrolleeAge;
    private int grade;
    private int sacramentalGroupId;
    private String sacramentalGroupName;

    // ----- Accessors -----
    public int getEnrolleeId() { return enrolleeId; }
    public void setEnrolleeId(int enrolleeId) { this.enrolleeId = enrolleeId; }

    public String getEnrolleeName() { return enrolleeName; }
    public void setEnrolleeName(String enrolleeName) { this.enrolleeName = enrolleeName; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getClassName() { return className; }
    public void setClassName(int className) { this.className = className; }

    public int getEnrolleeAge() { return enrolleeAge; }
    public void setEnrolleeAge(int enrolleeAge) { this.enrolleeAge = enrolleeAge; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public int getSacramentalGroupId() { return sacramentalGroupId; }
    public void setSacramentalGroupId(int sacramentalGroupId) { this.sacramentalGroupId = sacramentalGroupId; }

    public String getSacramentalGroupName() { return sacramentalGroupName; }
    public void setSacramentalGroupName(String sacramentalGroupName) { this.sacramentalGroupName = sacramentalGroupName; }
}
