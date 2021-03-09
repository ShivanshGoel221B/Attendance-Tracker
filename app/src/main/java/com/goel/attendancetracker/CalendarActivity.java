package com.goel.attendancetracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goel.attendancetracker.classes.ClassesModel;
import com.goel.attendancetracker.database.DatabaseHandler;
import com.goel.attendancetracker.database.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity implements EditAttendanceDialog.SubmitNewAttendance {

    private String classId, className, organisationName, focusedDate;
    TextView presentCounter, absentCounter;
    int present, absent;
    DatabaseHandler databaseHandler;
    ClassesModel openClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        focusedDate = getCurrentDate();
        initializeClassData();
        Objects.requireNonNull(getSupportActionBar()).setTitle(organisationName.toUpperCase());
        ((TextView) findViewById(R.id.class_name_calendar)).setText(className.toUpperCase());
        initializeDatabase();
        initializeViews();
        setAttendance();
    }


    private void initializeClassData() {
        Bundle classData = getIntent().getExtras();
        String[] dataArray = classData.getStringArray(Params.CLASS_DATA_ARRAY);
        organisationName = dataArray[0];
        classId = dataArray[1];
        className = dataArray[2];
    }

    private void initializeDatabase() {
        databaseHandler = new DatabaseHandler(CalendarActivity.this);
        SQLiteDatabase database = databaseHandler.getReadableDatabase();
        final String getCommand =  "SELECT * FROM " + "\"" + organisationName + "\"" + " WHERE " + "class_sno" + "=" + classId;
        Cursor cursor = database.rawQuery(getCommand, null);
        cursor.moveToFirst();
        String history = cursor.getString(4);
        cursor.close();
        database.close();
        openClass = new ClassesModel();
        openClass.setId(Integer.parseInt(classId));
        openClass.setClassHistory(history);
    }

    private void initializeViews() {
        CalendarView calendarView = findViewById(R.id.class_calendar);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            focusedDate = String.valueOf(year) + month + dayOfMonth;
            setAttendance();
        });
        presentCounter = findViewById(R.id.present_counter);
        absentCounter = findViewById(R.id.absent_counter);
    }

    private void setAttendance() {
        JSONObject history;
        try {
            history = new JSONObject(openClass.getClassHistory());
        } catch (JSONException e) {
            return;
        }
        // INITIALIZING INITIAL DATE HISTORY
        JSONArray dateHistory;
        try {
            dateHistory = (JSONArray) history.get(focusedDate);
        } catch (JSONException e) {
            dateHistory = new JSONArray();
            try {
                dateHistory.put(0,0);
                dateHistory.put(1,0);
            } catch (JSONException jsonException) {
                return;
            }
        }

        try {
            present = dateHistory.getInt(0);
            absent = dateHistory.getInt(1);
        } catch (JSONException e) {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        presentCounter.setText(String.valueOf(present));
        absentCounter.setText(String.valueOf(absent));
    }

    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.YEAR)) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE);
    }

    public void editAttendance(View view){
        EditAttendanceDialog.presentCount = present;
        EditAttendanceDialog.absentCount = absent;

        EditAttendanceDialog attendanceDialog = new EditAttendanceDialog();
        attendanceDialog.show(getSupportFragmentManager(), "edit attendance");
    }

    @Override
    public void updateAttendance(int newPresent, int newAbsent) {
        int[] attendance = new int[2];
        attendance[0] = newPresent - present;
        attendance[1] = newAbsent - absent;
        databaseHandler.markAttendance(organisationName, openClass, focusedDate, attendance);
        databaseHandler.refreshOverAttendance(organisationName);
        setAttendance();

        Toast toast = Toast.makeText(CalendarActivity.this, "Updated Successfully", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 20);
        toast.show();
    }
    //==========================================================================================================//

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CalendarActivity.this, OrganisationActivity.class));
    }
}