package org.servantscode.formation;

public class Program {
    private int id;
    private String name;
    private int groupId;
    private int coordinatorId;
    private String coordinatorName;
    private int registrations;

    // ----- Accessors -----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getCoordinatorId() { return coordinatorId; }
    public void setCoordinatorId(int coordinatorId) { this.coordinatorId = coordinatorId; }

    public String getCoordinatorName() { return coordinatorName; }
    public void setCoordinatorName(String coordinatorName) { this.coordinatorName = coordinatorName; }

    public int getRegistrations() { return registrations; }
    public void setRegistrations(int registrations) { this.registrations = registrations; }
}
