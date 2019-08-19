package org.servantscode.formation;

public class Registration {
    private int enrolleeId;
    private String enrolleeName;
    private int programId;
    private int sectionId;
    private String sectionName;
    private int enrolleeAge;
    private int grade;
    private int sacramentalGroupId;
    private String sacramentalGroupName;

    // ----- Accessors -----
    public int getEnrolleeId() { return enrolleeId; }
    public void setEnrolleeId(int enrolleeId) { this.enrolleeId = enrolleeId; }

    public String getEnrolleeName() { return enrolleeName; }
    public void setEnrolleeName(String enrolleeName) { this.enrolleeName = enrolleeName; }

    public int getProgramId() { return programId; }
    public void setProgramId(int programId) { this.programId = programId; }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public int getEnrolleeAge() { return enrolleeAge; }
    public void setEnrolleeAge(int enrolleeAge) { this.enrolleeAge = enrolleeAge; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public int getSacramentalGroupId() { return sacramentalGroupId; }
    public void setSacramentalGroupId(int sacramentalGroupId) { this.sacramentalGroupId = sacramentalGroupId; }

    public String getSacramentalGroupName() { return sacramentalGroupName; }
    public void setSacramentalGroupName(String sacramentalGroupName) { this.sacramentalGroupName = sacramentalGroupName; }
}
