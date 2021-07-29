package com.goel.attendancetracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.attendancetracker.R
import com.goel.attendancetracker.classes.ClassesModel
import com.goel.attendancetracker.database.DatabaseHandler
import com.goel.attendancetracker.database.Params
import com.goel.attendancetracker.dialogboxes.EditAttendanceDialog
import com.goel.attendancetracker.dialogboxes.EditAttendanceDialog.SubmitNewAttendance
import java.util.*

class CalendarActivity : AppCompatActivity(), SubmitNewAttendance {
    private lateinit var classId: String
    private lateinit var className: String
    private lateinit var organisationName: String
    private lateinit var focusedDate: String
    private lateinit var presentCounter: TextView
    private lateinit var absentCounter: TextView
    private var present = 0
    private var absent = 0
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var openClass: ClassesModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        focusedDate = currentDate
        initializeClassData()
        supportActionBar?.title = organisationName.uppercase(Locale.getDefault())
        findViewById<TextView>(R.id.class_name_calendar).text = className.uppercase(Locale.getDefault())
        initializeDatabase()
        initializeViews()
        setAttendance()
    }

    private fun initializeClassData() {
        val classData = intent.extras
        val dataArray = classData!!.getStringArray(Params.CLASS_DATA_ARRAY)
        organisationName = dataArray!![0]
        classId = dataArray[1]
        className = dataArray[2]
    }

    private fun initializeDatabase() {
        databaseHandler = DatabaseHandler(this@CalendarActivity)
        val database = databaseHandler.readableDatabase
        val getCommand = "SELECT * FROM \"$organisationName\" WHERE class_sno=$classId"
        val cursor = database.rawQuery(getCommand, null)
        cursor.moveToFirst()
        val history = cursor.getString(4)
        cursor.close()
        database.close()
        openClass = ClassesModel()
        openClass.id = classId.toInt()
        openClass.classHistory = history
    }

    private fun initializeViews() {
        val calendarView = findViewById<CalendarView>(R.id.class_calendar)
        calendarView.setOnDateChangeListener { _, year: Int, month: Int, dayOfMonth: Int ->
            focusedDate = getFormattedDate(year, month, dayOfMonth)
            setAttendance()
        }
        presentCounter = findViewById(R.id.present_counter)
        absentCounter = findViewById(R.id.absent_counter)
    }

    private fun setAttendance() {
        present = openClass.getPresentCount(focusedDate)
        absent = openClass.getAbsentCount(focusedDate)
        presentCounter.text = present.toString()
        absentCounter.text = absent.toString()
    }

    private val currentDate: String
        get() {
            val calendar = Calendar.getInstance()
            return getFormattedDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DATE]
            )
        }

    private fun getFormattedDate(year: Int, month: Int, date: Int): String {
        val formattedDate = if (date >= 10) date.toString() else "0$date"
        val formattedMonth = if (month >= 10) month.toString() else "0$month"
        return year.toString() + formattedMonth + formattedDate
    }

    fun editAttendance(view: View?) {
        EditAttendanceDialog.presentCount = present
        EditAttendanceDialog.absentCount = absent
        val attendanceDialog = EditAttendanceDialog()
        attendanceDialog.show(supportFragmentManager, "edit attendance")
    }

    override fun updateAttendance(newPresent: Int, newAbsent: Int) {
        val attendance = IntArray(2)
        attendance[0] = newPresent - present
        attendance[1] = newAbsent - absent
        databaseHandler.markAttendance(organisationName, openClass, focusedDate, attendance)
        databaseHandler.refreshOverAttendance(organisationName)
        setAttendance()
        val toast = Toast.makeText(this@CalendarActivity, "Updated Successfully", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 20)
        toast.show()
    }

    //==========================================================================================================//
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@CalendarActivity, OrganisationActivity::class.java))
    }
}