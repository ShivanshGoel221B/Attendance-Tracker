package com.goel.attendancetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goel.attendancetracker.classes.ClassesAdapter;
import com.goel.attendancetracker.classes.ClassesModel;
import com.goel.attendancetracker.database.DatabaseHandler;
import com.goel.attendancetracker.database.Params;
import com.goel.attendancetracker.downloadmanager.ClassDownloadManager;
import com.goel.attendancetracker.downloadmanager.FileDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class OrganisationActivity extends AppCompatActivity implements EditDialogBox.EditDialogListener, AddDialogBox.AddDialogListener, MarkDialogBox.MarkAttendanceActions {


    private static final int STORAGE_PERMISSION_TOKEN = 1;
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


    @SuppressLint("SetTextI18n")
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

        ((TextView)findViewById(R.id.target_value)).setText(overallRequiredAttendance + "%");

        setClickListeners();
    }

    //-- Context Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.organisation_toolbar_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_new_class:
                addNewClass();
                break;
            case R.id.delete_all_classes:
                deleteAllClasses();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //===========================================

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
                model.setClassCounter();
                classList.add(model);
                classAdapter.notifyItemInserted(classList.indexOf(model));
            }while (cursor.moveToNext());
        }

        cursor.close();
        readable.close();

    }

    //  ===============  ADD CLASS METHODS  =============== //
    public void addNewClass(){
        AddDialogBox addDialogBox = new AddDialogBox();
        addDialogBox.show(getSupportFragmentManager(), "add dialog");
        AddDialogBox.overallAttendance = overallRequiredAttendance;
    }

    private void deleteAllClasses(){
        new AlertDialog.Builder(OrganisationActivity.this)
                .setMessage("This will delete all the Classes in the organisation. Continue?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    for(ClassesModel model: classList)
                        databaseHandler.deleteClass(organisationName, String.valueOf(model.getId()));
                    classList.clear();
                    classAdapter.notifyDataSetChanged();
                    Toast.makeText(OrganisationActivity.this, "Deleted All Classes", Toast.LENGTH_LONG).show();
                    refreshProgress();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
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

            int classId = databaseHandler.addNewClass(organisationName, values);
            model.setId(classId);

            classList.add(model);
            classAdapter.notifyItemInserted(classList.indexOf(model));

            refreshProgress();
        }
    }

    //=================================================================================//


    // Click Listeners

    private void setClickListeners(){
        classAdapter.setOnItemClickListener(new ClassesAdapter.OnItemClickListener() {
            @Override
            public void onHistoryClick(int position) {
                Intent intent = new Intent(OrganisationActivity.this, CalendarActivity.class);
                String[] dataArray = {organisationName, String.valueOf(classList.get(position).getId()), classList.get(position).getClassName()};
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

            @Override
            public void onDownloadClick(int position) {
                focusedClass = classList.get(position);
                downloadClassAttendance();
            }
        });

    }
    
    //========== DOWNLOAD ATTENDANCE ============//

    private boolean hasStoragePermission(){
        return ContextCompat.checkSelfPermission(OrganisationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(OrganisationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_TOKEN){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                downloadClassAttendance();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadClassAttendance(){
        if (hasStoragePermission()){
            ClassDownloadManager classDownloadManager = new ClassDownloadManager(focusedClass, organisationName);
            setFileModelData();
            switch (classDownloadManager.downloadAttendance()){
                case ClassDownloadManager.DOWNLOAD_FAILED:
                    Toast.makeText(OrganisationActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                    break;
                case ClassDownloadManager.DOWNLOAD_SUCCESSFUL:
                    Toast.makeText(OrganisationActivity.this, "File Saved as " + classDownloadManager.getFilePath(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
        else{
            ActivityCompat.requestPermissions(OrganisationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_TOKEN);
        }
    }
    //=======================================================================//
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
    public void setCounters(TextView presentCounter, TextView absentCounter) {
        int presentCount, absentCount;
        JSONObject history;
        try {
            history = new JSONObject(focusedClass.getClassHistory());
        } catch (JSONException e) {
            return;
        }
        // INITIALIZING INITIAL DATE HISTORY
        JSONArray dateHistory;
        try {
            dateHistory = (JSONArray) history.get(getCurrentDate());
        } catch (JSONException e) {
            dateHistory = new JSONArray();
            try {
                dateHistory.put(0, 0);
                dateHistory.put(1, 0);
            } catch (JSONException ignored) {
            }
        }
        try {
            presentCount = dateHistory.getInt(0);
            absentCount = dateHistory.getInt(1);
        } catch (JSONException e) {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }
        presentCounter.setText(String.valueOf(presentCount));
        absentCounter.setText(String.valueOf(absentCount));
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

        ImageView flag = findViewById(R.id.target_flag);
        overallAttendance = databaseHandler.refreshOverAttendance(organisationName);

        ((TextView) findViewById(R.id.overall_percentage_counter)).setText(overallAttendance + "%");

        overallProgress.setProgress(0);

        if (overallAttendance >= overallRequiredAttendance) {
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress));
            flag.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00CF60")));
        }
        else if (overallAttendance > overallRequiredAttendance * 0.75f){
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress_low));
            flag.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F56600")));
        }
        else{
            overallProgress.setProgressDrawable(ContextCompat.getDrawable(OrganisationActivity.this, R.drawable.attendance_progress_danger));
            flag.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF073A")));
        }

        if (overallAttendance>0)
            overallProgress.setProgress(overallAttendance);
    }

    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        return getFormattedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    private String getFormattedDate(int year, int month, int date){
        String formattedDate = (date >= 10)? String.valueOf(date): "0" + date;
        String formattedMonth = (month >= 10)? String.valueOf(month): "0" + month;
        return year + formattedMonth + formattedDate;
    }

    private void setFileModelData() {
        FileDataModel.fonts = new Typeface[]{
                ResourcesCompat.getFont(OrganisationActivity.this, R.font.halant),
                ResourcesCompat.getFont(OrganisationActivity.this, R.font.halant_medium),
                ResourcesCompat.getFont(OrganisationActivity.this, R.font.halant_semibold),
                ResourcesCompat.getFont(OrganisationActivity.this, R.font.poly),
                ResourcesCompat.getFont(OrganisationActivity.this, R.font.adamina)
        };

        FileDataModel.logo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), 110, 110, false);
        FileDataModel.table = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.attendance_table), 700, 1000, false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrganisationActivity.this, MainActivity.class));
    }

}
