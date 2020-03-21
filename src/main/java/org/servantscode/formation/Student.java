package org.servantscode.formation;

import java.util.List;

public class Student {
    private int id;
    private int enrolleeId;
    private String enrolleeName;
    private int programId;
    private int sectionId;
    private String sectionName;
    private int enrolleeAge;
    private String schoolGrade;
    private int sacramentalGroupId;
    private String sacramentalGroupName;
    private List<String> parentNames;
    private List<String> parentPhones;
    private List<String> allergies;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getSchoolGrade() { return schoolGrade; }
    public void setSchoolGrade(String schoolGrade) { this.schoolGrade = schoolGrade; }

    public int getSacramentalGroupId() { return sacramentalGroupId; }
    public void setSacramentalGroupId(int sacramentalGroupId) { this.sacramentalGroupId = sacramentalGroupId; }

    public String getSacramentalGroupName() { return sacramentalGroupName; }
    public void setSacramentalGroupName(String sacramentalGroupName) { this.sacramentalGroupName = sacramentalGroupName; }

    public List<String> getParentNames() { return parentNames; }
    public void setParentNames(List<String> parentNames) { this.parentNames = parentNames; }

    public List<String> getParentPhones() { return parentPhones; }
    public void setParentPhones(List<String> parentPhones) { this.parentPhones = parentPhones; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
}
