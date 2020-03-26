package org.servantscode.formation;

import java.util.List;

public class Student extends Registration {
    private String email;
    private String phoneNumber;
    private List<String> parentNames;
    private List<String> parentPhones;
    private List<String> parentEmails;
    private List<String> allergies;

    // ----- Accessors -----
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<String> getParentNames() { return parentNames; }
    public void setParentNames(List<String> parentNames) { this.parentNames = parentNames; }

    public List<String> getParentPhones() { return parentPhones; }
    public void setParentPhones(List<String> parentPhones) { this.parentPhones = parentPhones; }

    public List<String> getParentEmails() { return parentEmails; }
    public void setParentEmails(List<String> parentEmails) { this.parentEmails = parentEmails; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
}
