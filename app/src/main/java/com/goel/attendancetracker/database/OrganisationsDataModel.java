package com.goel.attendancetracker.database;

public class OrganisationsDataModel {

    private String name;
    private int target;
    private int attendance;

    public OrganisationsDataModel()
    {
        this.name = "";
        this.target = 100;
        this.attendance = 100;
    }

    public OrganisationsDataModel(String name) {
        this.name = name;
        this.attendance = 100;
        this.target = 100;
    }

    public OrganisationsDataModel(String name, int target) {
        this.name = name;
        this.target = target;
        this.attendance = 100;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }
}
