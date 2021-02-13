package com.goel.attendancetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.goel.attendancetracker.classes.ClassesModel;


public class DatabaseHandler extends SQLiteOpenHelper {


    public DatabaseHandler(Context context) {
        super(context, Params.DB_NAME, null, Params.DB_VERSION);
        this.getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + Params.ROOT_TABLE;
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void deleteOrganisation(String organisationId, String organisationName)
    {
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.delete(Params.ORGANISATIONS, "s_no=?", new String[]{organisationId});
        writableDatabase.execSQL("DROP TABLE IF EXISTS " + "\"" + organisationName + "\"");
        writableDatabase.close();
    }

    public void updateOrganisation(ContentValues values, String organisationId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.update(Params.ORGANISATIONS, values, "s_no=?", new String[]{organisationId});
        writableDatabase.close();
    }

    public void createNewOrganisation(OrganisationsDataModel newOrganisation){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        String tableCommand = "CREATE TABLE " + "\"" + newOrganisation.getName() + "\"" + Params.NEW_TABLE;
        writableDatabase.execSQL(tableCommand);
        writableDatabase.close();
    }

    public void insertOrganisation(ContentValues values){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.insert(Params.ORGANISATIONS, null, values);
        writableDatabase.close();
    }

    // CLASSES METHOD

    public void deleteClass(String organisationName, String classId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.delete("\"" + organisationName + "\"", "class_sno=?", new String[]{classId});
        writableDatabase.close();
    }

    public void addNewClass(String name, ContentValues values){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.insert( "\"" + name + "\"", null, values);
        writableDatabase.close();
    }

    public void updateClass(String organisationName, ContentValues values, String classId){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();
        writableDatabase.update("\"" + organisationName + "\"", values, "class_sno=?", new String[]{classId});
        writableDatabase.close();
    }

    public void markAttendance(String organisationName, ClassesModel model, String markDate, int[] attendance){

        SQLiteDatabase writableDatabase = this.getWritableDatabase();

        writableDatabase.close();
    }

}
