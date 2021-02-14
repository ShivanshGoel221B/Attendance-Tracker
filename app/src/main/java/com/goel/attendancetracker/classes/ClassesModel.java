package com.goel.attendancetracker.classes;

public class ClassesModel {
    private int id;
    private String className;
    private int classAttendancePercentage;
    private int requiredAttendance;
    private String classHistory;

    public ClassesModel() {
        this.className = " ";
        this.classAttendancePercentage = 100;
        this.requiredAttendance = 100;
        this.classHistory = "{}";
    }

    public ClassesModel(String className, int requiredAttendance) {
        this.className = className;
        this.classAttendancePercentage = 100;
        this.requiredAttendance = requiredAttendance;
        this.classHistory = "{}";
    }

    public int getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public int getClassAttendancePercentage() {
        return classAttendancePercentage;
    }

    public int getRequiredAttendance() {
        return requiredAttendance;
    }

    public String getClassHistory() {
        return classHistory;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setClassAttendancePercentage(int classAttendancePercentage) {
        this.classAttendancePercentage = classAttendancePercentage;
    }

    public void setRequiredAttendance(int requiredAttendance) {
        this.requiredAttendance = requiredAttendance;
    }

    public void setClassHistory(String classHistory) {
        this.classHistory = classHistory;
    }
}
