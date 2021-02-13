package com.goel.attendancetracker.classes;

import com.goel.attendancetracker.R;

public class ClassesModel {
    private int id;
    private String className;
    private int classAttendancePercentage;
    private int requiredAttendance;
    private String classHistory;
    private final int classEditIcon;
    private final int classDeleteIcon;

    public ClassesModel() {
        this.className = " ";
        this.classAttendancePercentage = 100;
        this.requiredAttendance = 100;
        this.classHistory = "{}";
        this.classEditIcon = R.drawable.icon_edit;
        this.classDeleteIcon = R.drawable.icon_delete;
    }

    public ClassesModel(String className, int requiredAttendance) {
        this.className = className;
        this.classAttendancePercentage = 100;
        this.requiredAttendance = requiredAttendance;
        this.classHistory = "{}";
        this.classEditIcon = R.drawable.icon_edit;
        this.classDeleteIcon = R.drawable.icon_delete;
    }

    public ClassesModel(String className, int classAttendancePercentage, int requiredAttendance, String classHistory) {
        this.className = className;
        this.classAttendancePercentage = classAttendancePercentage;
        this.requiredAttendance = requiredAttendance;
        this.classHistory = classHistory;
        this.classEditIcon = R.drawable.icon_edit;
        this.classDeleteIcon = R.drawable.icon_delete;
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

    public int getClassEditIcon() {
        return classEditIcon;
    }

    public int getClassDeleteIcon() {
        return classDeleteIcon;
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
