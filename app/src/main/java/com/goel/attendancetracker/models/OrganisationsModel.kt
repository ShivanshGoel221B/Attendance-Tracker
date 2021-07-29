package com.goel.attendancetracker.organisations;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.goel.attendancetracker.R;

public class OrganisationsModel {

    private int id;
    private String organisationName;
    private int organisationAttendancePercentage;
    private int requiredAttendance;
    private final int organisationEditIcon;
    private final int organisationDeleteIcon;
    private final int markAttendanceIcon;

    public  OrganisationsModel()
    {
        this.organisationName = "";
        this.organisationAttendancePercentage = 100;
        this.requiredAttendance = 100;
        this.organisationEditIcon = R.drawable.icon_edit;
        this.organisationDeleteIcon = R.drawable.icon_delete;
        this.markAttendanceIcon = R.drawable.icon_mark;
    }

    public OrganisationsModel(String organisationName, int requiredAttendance) {
        this.organisationName = organisationName;
        this.organisationAttendancePercentage = 100;
        this.requiredAttendance = requiredAttendance;
        this.organisationEditIcon = R.drawable.icon_edit;
        this.organisationDeleteIcon = R.drawable.icon_delete;
        this.markAttendanceIcon = R.drawable.icon_mark;
    }

    public OrganisationsModel(String organisationName, int organisationAttendancePercentage, int requiredAttendance) {
        this.organisationName = organisationName;
        this.organisationAttendancePercentage = organisationAttendancePercentage;
        this.requiredAttendance = requiredAttendance;
        this.organisationEditIcon = R.drawable.icon_edit;
        this.organisationDeleteIcon = R.drawable.icon_delete;
        this.markAttendanceIcon = R.drawable.icon_mark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public int getOrganisationAttendancePercentage() {
        return organisationAttendancePercentage;
    }

    public void setOrganisationAttendancePercentage(int organisationAttendancePercentage) {
        this.organisationAttendancePercentage = organisationAttendancePercentage;
    }

    public int getRequiredAttendance() {
        return requiredAttendance;
    }

    public void setRequiredAttendance(int requiredAttendance) {
        this.requiredAttendance = requiredAttendance;
    }

    public int getOrganisationEditIcon() {
        return organisationEditIcon;
    }

    public int getOrganisationDeleteIcon() {
        return organisationDeleteIcon;
    }

    public int getMarkAttendanceIcon() {
        return markAttendanceIcon;
    }
}
