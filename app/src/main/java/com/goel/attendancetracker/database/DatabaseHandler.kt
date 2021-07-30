package com.goel.attendancetracker.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.goel.attendancetracker.models.ClassesModel
import com.goel.attendancetracker.models.OrganisationsModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, Params.DB_NAME, null, Params.DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE ${Params.ROOT_TABLE}"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun deleteOrganisation(organisationId: String, organisationName: String) {
        val writableDatabase = this.writableDatabase
        writableDatabase.delete(Params.ORGANISATIONS, "s_no=?", arrayOf(organisationId))
        writableDatabase.execSQL("DROP TABLE IF EXISTS \"$organisationName\"")
        writableDatabase.close()
    }

    fun updateOrganisation(values: ContentValues, organisationName: String) {
        val writableDatabase = this.writableDatabase
        writableDatabase.update(Params.ORGANISATIONS, values, "name=?", arrayOf(organisationName))
        writableDatabase.close()
    }

    fun renameOrganisationTable(oldName: String, newName: String) {
        val database = this.writableDatabase
        database.execSQL("ALTER TABLE \"$oldName\" RENAME TO \"$newName\"")
        database.close()
    }

    fun createNewOrganisation(newOrganisation: OrganisationsModel) {
        try {
            this.writableDatabase.use { writableDatabase ->
                val tableCommand =
                    "CREATE TABLE \"${newOrganisation.name}\"${Params.NEW_TABLE}"
                writableDatabase.execSQL(tableCommand)
            }
        } catch (e: SQLiteException) {
            throw SQLiteException()
        }
    }

    fun insertOrganisation(values: ContentValues) {
        val writableDatabase = this.writableDatabase
        writableDatabase.insert(Params.ORGANISATIONS, null, values)
        writableDatabase.close()
    }

    // CLASSES METHOD
    fun deleteClass(organisationName: String, classId: String) {
        val writableDatabase = this.writableDatabase
        writableDatabase.delete("\"$organisationName\"", "class_sno=?", arrayOf(classId))
        writableDatabase.close()
    }

    fun addNewClass(name: String, values: ContentValues): Int {
        val writableDatabase = this.writableDatabase
        val readableDatabase = this.readableDatabase
        writableDatabase.insert("\"$name\"", null, values)
        @SuppressLint("Recycle") val cursor =
            readableDatabase.rawQuery("SELECT * FROM\"$name\"ORDER BY class_sno DESC LIMIT 1", null)
        cursor.moveToFirst()
        val id = cursor.getInt(0)
        readableDatabase.close()
        writableDatabase.close()
        return id
    }

    fun updateClass(organisationName: String, values: ContentValues, classId: String) {
        val writableDatabase = this.writableDatabase
        writableDatabase.update(
            "\"$organisationName\"",
            values,
            "class_sno=?",
            arrayOf(classId)
        )
        writableDatabase.close()
    }

    fun markAttendance(
        organisationName: String,
        model: ClassesModel,
        markDate: String,
        attendance: IntArray
    ) {
        val history: JSONObject = try {
            JSONObject(model.classHistory)
        } catch (e: JSONException) {
            return
        }
        // INITIALIZING INITIAL DATE HISTORY
        var dateHistory: JSONArray
        try {
            dateHistory = history[markDate] as JSONArray
        } catch (e: JSONException) {
            dateHistory = JSONArray()
            try {
                dateHistory.put(0, 0)
                dateHistory.put(1, 0)
            } catch (jsonException: JSONException) {
                return
            }
        }

        // UPDATE MODEL HISTORY
        try {
            dateHistory.put(0, attendance[0] + dateHistory.getInt(0))
            dateHistory.put(1, attendance[1] + dateHistory.getInt(1))
        } catch (e: JSONException) {
            return
        }
        try {
            history.put(markDate, dateHistory)
            model.classHistory = history.toString()
        } catch (e: JSONException) {
            return
        }

        // UPDATE NEW ATTENDANCE
        var present = 0
        var absent = 0
        try {
            val keys = history.keys()
            while (keys.hasNext()) {
                val dateData = history[keys.next()] as JSONArray
                present += dateData.getInt(0)
                absent += dateData.getInt(1)
            }
        } catch (e: JSONException) {
            return
        }
        var newAttendance = 100
        if (present != 0 || absent != 0) {
            newAttendance = present * 100 / (present + absent)
        }
        model.classAttendancePercentage = newAttendance
        model.setClassCounter()

        // UPDATE IN DATABASE
        val writableDatabase = this.writableDatabase
        val values = ContentValues()
        values.put(Params.HISTORY, history.toString())
        values.put(Params.ATTENDANCE, newAttendance)
        updateClass(organisationName, values, java.lang.String.valueOf(model.id))
        writableDatabase.close()
    }

    fun refreshOverAttendance(organisationName: String): Int {
        var overallAttendance = 100
        val readable = this.readableDatabase
        val getCommand = "SELECT * FROM \"$organisationName\""
        @SuppressLint("Recycle") val cursor = readable.rawQuery(getCommand, null)
        var sum = 0
        var counter = 0
        if (cursor.moveToFirst()) {
            do {
                sum += cursor.getInt(2)
                counter++
            } while (cursor.moveToNext())
        }
        if (counter > 0) {
            overallAttendance = sum / counter
        }
        cursor.close()
        readable.close()
        val newOrganisationValues = ContentValues()
        newOrganisationValues.put(Params.ATTENDANCE, overallAttendance)
        updateOrganisation(newOrganisationValues, organisationName)
        return overallAttendance
    }

    init {
        this.writableDatabase
    }
}