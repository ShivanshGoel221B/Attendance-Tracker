package com.goel.attendancetracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goel.attendancetracker.classes.ClassesAdapter;
import com.goel.attendancetracker.classes.ClassesModel;
import com.goel.attendancetracker.database.DatabaseHandler;
import com.goel.attendancetracker.database.Params;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class OrganisationActivity extends AppCompatActivity implements EditDialogBox.EditDialogListener, AddDialogBox.AddDialogListener, MarkDialogBox.SubmitAttendance {

    private String organisationName;
    private String organisationId;
    private DatabaseHandler databaseHandler;
    private ProgressBar overallProgress;
    private int overallAttendance;
    private int overallRequiredAttendance;

    private RecyclerView classContainer;
    private ClassesAdapter classAdapter;
    private ArrayList<ClassesModel> classList;
    private ClassesModel focusedClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisation);

        organisationId = Params.OPEN_ORG;

        classContainer = findViewById(R.id.class_grid);
        classList = new ArrayList<>();

        databaseHandler = new DatabaseHandler(OrganisationActivity.this);

        initializeOrganisation();
        Objects.requireNonNull(getSupportActionBar()).setTitle(organisationName.toUpperCase());

        setAdapter();
        getClassList();
        refreshProgress();
        setClickListeners();
    }

    private void setAdapter() {
        classAdapter = new ClassesAdapter(classList, this);
        classContainer.setAdapter(classAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        classContainer.setLayoutManager(layoutManager);
    }

    private void getClassList() {
        SQLiteDatabase readable = databaseHandler.getReadableDatabase();
        String getCommand = "SELECT * FROM " + "\"" + organisationName + "\"";
        @SuppressLint("Recycle") Cursor cursor = readable.rawQuery(getCommand, null);

        if (cursor.moveToFirst())
        {
            do {
                ClassesModel model = new ClassesModel();
                model.setId(cursor.getInt(0));
                model.setClassName(cursor.getString(1));
                model.setClassAttendancePercentage(cursor.getInt(2));
                model.setRequiredAttendance(cursor.getInt(3));
                model.setClassHistory(cursor.getString(4));
                classList.add(model);
                classAdapter.notifyItemInserted(classList.indexOf(model));
            }while (cursor.moveToNext());
        }

        cursor.close();
        readable.close();

    }

    //  ===============  ADD CLASS METHODS  =============== //
    public void addClassButton(View view){
        AddDialogBox addDialogBox = new AddDialogBox();
        addDialogBox.show(getSupportFragmentManager(), "add dialog");
        AddDialogBox.overallAttendance = overallRequiredAttendance;
    }

    @Override
    public void addSubmitDetails(EditText newNameText, EditText newTargetText) {
        if (isDataValid(newNameText, newTargetText)){
            ContentValues values = new ContentValues();
            ClassesModel model = new ClassesModel(newNameText.getText().toString(), Integer.parseInt(newTargetText.getText().toString()));
            values.put(Params.NAME, model.getClassName());
            values.put(Params.TARGET, model.getRequiredAttendance());
            values.put(Params.ATTENDANCE, model.getClassAttendancePercentage());
            values.put(Params.HISTORY, model.getClassHistory());

            databaseHandler.addNewClass(organisationName, values);

            classList.add(model);
            classAdapter.notifyItemInserted(classList.indexOf(model));

            refreshProgress();
        }
    }

    //=================================================================================//


    // Methods for overall progress

    private void setClickListeners(){
        classAdapter.setOnItemClickListener(new ClassesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(OrganisationActivity.this, CalendarActivity.class);
                String[] dataArray = {organisationName, String.valueOf(classList.get(position).getId())};
                intent.putExtra(Params.CLASS_DATA_ARRAY, dataArray);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteClass(position);
            }

            @Override
            public void onEditClick(int position) {
                focusedClass = classList.get(position);
                editClass();
            }

            @Override
            public void onMarkClick(int position) {
                markAttendance(position);
            }
        });

    }


    // Button methods

    private void markAttendance(int position) {
        MarkDialogBox markDialogBox = new MarkDialogBox();
        markDialogBox.show(getSupportFragmentManager(), "mark attendance");
        focusedClass = classList.get(position);
    }

    private void editClass() {
        EditDialogBox editDialogBox = new EditDialogBox();
        editDialogBox.show(getSupportFragmentManager(), "edit dialog");
        EditDialogBox.name = focusedClass.getClassName();
        EditDialogBox.target = focusedClass.getRequiredAttendance();
    }

    private void deleteClass(int position){
        new AlertDialog.Builder(OrganisationActivity.this)
                    .setMessage("Are you sure you want to delete " + classList.get(position).getClassName() + " ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseHandler.deleteClass(organisationName, String.valueOf(classList.get(position).getId()));
                    classAdapter.notifyItemRemoved(position);
                    Toast.makeText(OrganisationActivity.this, "Deleted " + classList.get(position).getClassName(), Toast.LENGTH_LONG).show();
                    classList.remove(position);
                    refreshProgress();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    //==================================================================//
    @SuppressLint("SetTextI18n")
    private void initializeOrganisation() {
        ProgressBar requiredOverallProgress = findViewById(R.id.overall_required_attendance);
        overallProgress = findViewById(R.id.overall_attendance);
        final String getCommand =  "SELECT * FROM " + Params.ORGANISATIONS + " WHERE " + "s_no" + "=" + organisationId;
        SQLiteDatabase rootReadable = databaseHandler.getReadableDatabase();
        Cursor cursor = rootReadable.rawQuery(getCommand, null);
        cursor.moveToFirst();
        organisationName = cursor.getString(1);
        overallAttendance = cursor.getInt(2);
        overallRequiredAttendance = cursor.getInt(3);

        requiredOverallProgress.setProgress(overallRequiredAttendance);

        cursor.close();
        rootReadable.close();
    }

    @Override
    public void submitAttendance(int presentCount, int absentCount) {

        int[] dateHistory = new int[2];
        dateHistory[0] = (presentCount);
        dateHistory[1] = (absentCount);

        databaseHandler.markAttendance(organisationName, focusedClass, getCurrentDate(), dateHistory);
        classAdapter.notifyItemChanged(classList.indexOf(focusedClass));
        refreshProgress();

        Toast.makeText(this, "Attendance Marked", Toast.LENGTH_SHORT).show();
        focusedClass = null;
    }

    @Override
    public void submitDetails(EditText newNameText, EditText newTargetText) {
        if (isDataValid(newNameText, newTargetText))
        {
            ContentValues values = new ContentValues();
            values.put(Params.NAME, newNameText.getText().toString());
            values.put(Params.TARGET, Integer.parseInt(newTargetText.getText().toString()));

            databaseHandler.updateClass(organisationName, values, String.valueOf(focusedClass.getId()));

            focusedClass.setClassName(newNameText.getText().toString());
            focusedClass.setRequiredAttendance(Integer.parseInt(newTargetText.getText().toString()));
            classAdapter.notifyItemChanged(classList.indexOf(focusedClass));
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show();
            focusedClass = null;
        }
    }

    private boolean isDataValid(EditText newNameText, EditText newTargetText) {
        String name;
        int target;
        try {
            name = newNameText.getText().toString();
        } catch (Exception e) {
            name = "";
        }
        try {
            target = Integer.parseInt(newTargetText.getText().toString());
        } catch (NumberFormatException e) {
            target = 101;
        }
        if (name.isEmpty()) {
            newNameText.setError("Please Enter a valid name");
            return false;
        }
        if (name.length()>30){
            newNameText.setError("The length of the name should be less than or equal to 30");
            return false;
        }
        if (target>100 || target<0){
            newTargetText.setError("Enter a valid number from 0 to 100");
            return false;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void refreshProgress() {

        int sum = 0;
        for (ClassesModel classesModel : classList) {
            sum += classesModel.getClassAttendancePercentage();
        }
        try {
            overallAttendance = sum/classAdapter.getItemCount();
        } catch (ArithmeticException e) {
            overallAttendance = 100;
        }

        ContentValues newOrganisationValues = new ContentValues();
        newOrganisationValues.put(Params.ATTENDANCE, overallAttendance);
        databaseHandler.updateOrganisation(newOrganisationValues, organisationId);


        ((TextView) findViewById(R.id.overall_percentage_counter)).setText(overallAttendance + "%");

        overallProgress.setProgress(0);

        if (overallAttendance >= overallRequiredAttendance)
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress));
        else if (overallAttendance > overallRequiredAttendance * 0.75f)
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress_low));
        else
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress_danger));

        overallProgress.setProgress(overallAttendance);

    }

    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.YEAR)) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrganisationActivity.this, MainActivity.class));
    }

}
