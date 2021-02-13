package com.goel.attendancetracker.database;

public class ClassDataModel {
    private String name;
    private int target;
    private int attendance;
    private String history;

    public ClassDataModel(String name) {
        this.name = name;
        this.attendance = 0;
        this.target = 100;
        this.history = "";
    }

    public ClassDataModel(String name, int target) {
        this.name = name;
        this.target = target;
        this.attendance = 0;
        this.history = "";
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

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
