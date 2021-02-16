package com.goel.attendancetracker.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ClassesModel {
    private int id;
    private String className;
    private String classCounter;
    private int classAttendancePercentage;
    private int requiredAttendance;
    private String classHistory;

    public ClassesModel() {
        this.className = " ";
        this.classCounter = "0/0";
        this.classAttendancePercentage = 100;
        this.requiredAttendance = 100;
        this.classHistory = "{}";
    }

    public ClassesModel(String className, int requiredAttendance) {
        this.className = className;
        this.classCounter = "0/0";
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

    public String getClassCounter() {
        return classCounter;
    }

    public void setClassCounter(){
        JSONObject history;
        try {
            history = new JSONObject(this.getClassHistory());
        } catch (JSONException e) {
            this.classCounter = "0/0";
            return;
        }
        // UPDATE NEW ATTENDANCE
        int present = 0;
        int absent = 0;
        try {
            Iterator<String> keys = history.keys();
            while (keys.hasNext()){
                JSONArray dateData = (JSONArray) history.get(keys.next());
                present += dateData.getInt(0);
                absent += dateData.getInt(1);
            }
            int total = present + absent;
            this.classCounter = present + "/" + total;
        }catch (JSONException e) {
            this.classCounter = "0/0";
        }
    }
}
