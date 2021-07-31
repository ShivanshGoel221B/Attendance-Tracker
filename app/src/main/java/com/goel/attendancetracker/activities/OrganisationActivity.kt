package com.goel.attendancetracker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.attendancetracker.R
import com.goel.attendancetracker.adapters.ClassesAdapter
import com.goel.attendancetracker.utils.database.DatabaseHandler
import com.goel.attendancetracker.utils.Constants
import com.goel.attendancetracker.dialogboxes.AddDialogBox
import com.goel.attendancetracker.dialogboxes.AddDialogBox.AddDialogListener
import com.goel.attendancetracker.dialogboxes.EditDialogBox
import com.goel.attendancetracker.dialogboxes.EditDialogBox.EditDialogListener
import com.goel.attendancetracker.dialogboxes.MarkDialogBox
import com.goel.attendancetracker.dialogboxes.MarkDialogBox.MarkAttendanceListener
import com.goel.attendancetracker.downloadmanager.ClassDownloadManager
import com.goel.attendancetracker.downloadmanager.FileDataModel
import com.goel.attendancetracker.models.ClassesModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class OrganisationActivity : AppCompatActivity(), EditDialogListener, AddDialogListener,
    MarkAttendanceListener {
    private lateinit var organisationName: String
    private lateinit var organisationId: String
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var overallProgress: ProgressBar
    private var overallAttendance = 0
    private var overallRequiredAttendance = 0
    private lateinit var classContainer: RecyclerView
    private lateinit var classAdapter: ClassesAdapter
    private lateinit var classList: ArrayList<ClassesModel>
    private var focusedClass: ClassesModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organisation)
        organisationId = Constants.OPEN_ORG!!
        classContainer = findViewById(R.id.class_grid)
        classList = ArrayList()
        databaseHandler = DatabaseHandler(this@OrganisationActivity)
        initializeOrganisation()
        supportActionBar?.title = organisationName.uppercase(Locale.getDefault())
        setAdapter()
        getClassList()
        refreshProgress()
        findViewById<TextView>(R.id.target_value).text = "$overallRequiredAttendance%"
        setClickListeners()
    }

    //-- Context Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.organisation_toolbar_menu, menu)
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_new_class -> addNewClass()
            R.id.delete_all_classes -> deleteAllClasses()
        }
        return super.onOptionsItemSelected(item)
    }

    //===========================================
    private fun setAdapter() {
        classAdapter = ClassesAdapter(classList, this)
        classContainer.adapter = classAdapter
        val layoutManager = GridLayoutManager(this, 2)
        classContainer.layoutManager = layoutManager
    }

    private fun getClassList() {
        val readable = databaseHandler.readableDatabase
        val getCommand = "SELECT * FROM \"$organisationName\""
        @SuppressLint("Recycle") val cursor = readable.rawQuery(getCommand, null)
        if (cursor.moveToFirst()) {
            do {
                val model = ClassesModel()
                model.id = cursor.getInt(0)
                model.className = cursor.getString(1)
                model.classAttendancePercentage = cursor.getInt(2)
                model.requiredAttendance = cursor.getInt(3)
                model.classHistory = cursor.getString(4)
                model.setClassCounter()
                classList.add(model)
                classAdapter.notifyItemInserted(classList.indexOf(model))
            } while (cursor.moveToNext())
        }
        cursor.close()
        readable.close()
    }

    //  ===============  ADD CLASS METHODS  =============== //
    private fun addNewClass() {
        val addDialogBox = AddDialogBox(this)
        addDialogBox.show(supportFragmentManager, "add dialog")
        AddDialogBox.overallAttendance = overallRequiredAttendance
    }

    private fun deleteAllClasses() {
        AlertDialog.Builder(this@OrganisationActivity)
            .setMessage("This will delete all the Classes in the organisation. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                for (model in classList) databaseHandler.deleteClass(
                    organisationName,
                    model.id.toString()
                )
                classList.clear()
                classAdapter.notifyDataSetChanged()
                Toast.makeText(this@OrganisationActivity, "Deleted All Classes", Toast.LENGTH_LONG)
                    .show()
                refreshProgress()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .show()
    }

    override fun addSubmitDetails(newNameText: EditText, newTargetText: EditText) {
        if (isDataValid(newNameText, newTargetText)) {
            val values = ContentValues()
            val model =
                ClassesModel(newNameText.text.toString(), newTargetText.text.toString().toInt())
            values.put(Constants.NAME, model.className)
            values.put(Constants.TARGET, model.requiredAttendance)
            values.put(Constants.ATTENDANCE, model.classAttendancePercentage)
            values.put(Constants.HISTORY, model.classHistory)
            val classId = databaseHandler.addNewClass(organisationName, values)
            model.id = classId
            classList.add(model)
            classAdapter.notifyItemInserted(classList.indexOf(model))
            refreshProgress()
        }
    }

    //=================================================================================//
    // Click Listeners
    private fun setClickListeners() {
        classAdapter.setOnItemClickListener(object : ClassesAdapter.OnItemClickListener {
            override fun onHistoryClick(position: Int) {
                val intent = Intent(this@OrganisationActivity, CalendarActivity::class.java)
                val dataArray = arrayOf(
                    organisationName, classList[position]
                        .id.toString(), classList[position].className
                )
                intent.putExtra(Constants.CLASS_DATA_ARRAY, dataArray)
                startActivity(intent)
            }

            override fun onDeleteClick(position: Int) {
                deleteClass(position)
            }

            override fun onEditClick(position: Int) {
                focusedClass = classList[position]
                editClass()
            }

            override fun onMarkClick(position: Int) {
                markAttendance(position)
            }

            override fun onDownloadClick(position: Int) {
                focusedClass = classList[position]
                downloadClassAttendance()
            }
        })
    }

    //========== DOWNLOAD ATTENDANCE ============//
    private fun hasStoragePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this@OrganisationActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this@OrganisationActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_TOKEN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) downloadClassAttendance() else Toast.makeText(
                this,
                "Permission Denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun downloadClassAttendance() {
        if (hasStoragePermission()) {
            val classDownloadManager = ClassDownloadManager(focusedClass!!, organisationName)
            setFileModelData()
            when (classDownloadManager.downloadAttendance()) {
                ClassDownloadManager.DOWNLOAD_FAILED -> Toast.makeText(
                    this@OrganisationActivity,
                    "Download Failed",
                    Toast.LENGTH_SHORT
                ).show()
                ClassDownloadManager.DOWNLOAD_SUCCESSFUL -> Toast.makeText(
                    this@OrganisationActivity,
                    "File Saved as ${classDownloadManager.filePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                this@OrganisationActivity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_TOKEN
            )
        }
    }

    //=======================================================================//
    // Button methods
    private fun markAttendance(position: Int) {
        val markDialogBox = MarkDialogBox(this)
        markDialogBox.show(supportFragmentManager, "mark attendance")
        focusedClass = classList[position]
    }

    private fun editClass() {
        val editDialogBox = EditDialogBox(this)
        editDialogBox.show(supportFragmentManager, "edit dialog")
        EditDialogBox.name = focusedClass!!.className
        EditDialogBox.target = focusedClass!!.requiredAttendance
    }

    private fun deleteClass(position: Int) {
        AlertDialog.Builder(this@OrganisationActivity)
            .setMessage("Are you sure you want to delete " + classList[position].className + " ?")
            .setPositiveButton("Yes") { _, _ ->
                databaseHandler.deleteClass(
                    organisationName, classList[position]
                        .id.toString()
                )
                classAdapter.notifyItemRemoved(position)
                Toast.makeText(
                    this@OrganisationActivity,
                    "Deleted " + classList[position].className,
                    Toast.LENGTH_LONG
                ).show()
                classList.removeAt(position)
                refreshProgress()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .show()
    }

    //==================================================================//
    @SuppressLint("SetTextI18n")
    private fun initializeOrganisation() {
        val requiredOverallProgress = findViewById<ProgressBar>(R.id.overall_required_attendance)
        overallProgress = findViewById(R.id.overall_attendance)
        val getCommand =
            "SELECT * FROM " + Constants.ORGANISATIONS + " WHERE " + "s_no" + "=" + organisationId
        val rootReadable = databaseHandler.readableDatabase
        val cursor = rootReadable.rawQuery(getCommand, null)
        cursor.moveToFirst()
        organisationName = cursor.getString(1)
        overallAttendance = cursor.getInt(2)
        overallRequiredAttendance = cursor.getInt(3)
        requiredOverallProgress.progress = overallRequiredAttendance
        cursor.close()
        rootReadable.close()
    }

    override fun setCounters(presentCounter: TextView, absentCounter: TextView) {
        val presentCount: Int
        val absentCount: Int
        val history: JSONObject = try {
            JSONObject(focusedClass!!.classHistory)
        } catch (e: JSONException) {
            return
        }
        // INITIALIZING INITIAL DATE HISTORY
        var dateHistory: JSONArray
        try {
            dateHistory = history[currentDate] as JSONArray
        } catch (e: JSONException) {
            dateHistory = JSONArray()
            try {
                dateHistory.put(0, 0)
                dateHistory.put(1, 0)
            } catch (ignored: JSONException) {
            }
        }
        try {
            presentCount = dateHistory.getInt(0)
            absentCount = dateHistory.getInt(1)
        } catch (e: JSONException) {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
            return
        }
        presentCounter.text = presentCount.toString()
        absentCounter.text = absentCount.toString()
    }

    override fun submitAttendance(presentCount: Int, absentCount: Int) {
        val dateHistory = IntArray(2)
        dateHistory[0] = presentCount
        dateHistory[1] = absentCount
        focusedClass?.let {
            databaseHandler.markAttendance(organisationName, it, currentDate, dateHistory)
        }
        classAdapter.notifyItemChanged(classList.indexOf(focusedClass))
        refreshProgress()
        Toast.makeText(this, "Attendance Marked", Toast.LENGTH_SHORT).show()
        focusedClass = null
    }

    override fun submitDetails(newNameText: EditText, newTargetText: EditText) {
        if (isDataValid(newNameText, newTargetText)) {
            val values = ContentValues()
            values.put(Constants.NAME, newNameText.text.toString())
            values.put(Constants.TARGET, newTargetText.text.toString().toInt())
            databaseHandler.updateClass(organisationName, values, focusedClass!!.id.toString())
            focusedClass!!.className = newNameText.text.toString()
            focusedClass!!.requiredAttendance = newTargetText.text.toString().toInt()
            classAdapter.notifyItemChanged(classList.indexOf(focusedClass))
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show()
            focusedClass = null
        }
    }

    private fun isDataValid(newNameText: EditText, newTargetText: EditText): Boolean {
        val name: String = try {
            newNameText.text.toString()
        } catch (e: Exception) {
            ""
        }
        val target: Int = try {
            newTargetText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            101
        }
        if (name.isEmpty()) {
            newNameText.error = "Please Enter a valid name"
            return false
        }
        if (name.length > 30) {
            newNameText.error = "The length of the name should be less than or equal to 30"
            return false
        }
        if (target > 100 || target < 0) {
            newTargetText.error = "Enter a valid number from 0 to 100"
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun refreshProgress() {
        val flag = findViewById<ImageView>(R.id.target_flag)
        overallAttendance = databaseHandler.refreshOverAttendance(organisationName)
        (findViewById<View>(R.id.overall_percentage_counter) as TextView).text =
            "$overallAttendance%"
        overallProgress.progress = 0
        when {
            overallAttendance >= overallRequiredAttendance -> {
                overallProgress.progressDrawable =
                    ContextCompat.getDrawable(this@OrganisationActivity, R.drawable.attendance_progress)
                flag.imageTintList = ColorStateList.valueOf(Color.parseColor("#00CF60"))
            }
            overallAttendance > overallRequiredAttendance * 0.75f -> {
                overallProgress.progressDrawable = ContextCompat.getDrawable(
                    this@OrganisationActivity,
                    R.drawable.attendance_progress_low
                )
                flag.imageTintList = ColorStateList.valueOf(Color.parseColor("#F56600"))
            }
            else -> {
                overallProgress.progressDrawable = ContextCompat.getDrawable(
                    this@OrganisationActivity,
                    R.drawable.attendance_progress_danger
                )
                flag.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF073A"))
            }
        }
        if (overallAttendance > 0) overallProgress.progress = overallAttendance
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

    private fun setFileModelData() {
        FileDataModel.fonts = arrayOf(
            ResourcesCompat.getFont(this@OrganisationActivity, R.font.halant),
            ResourcesCompat.getFont(this@OrganisationActivity, R.font.halant_medium),
            ResourcesCompat.getFont(this@OrganisationActivity, R.font.halant_semibold),
            ResourcesCompat.getFont(this@OrganisationActivity, R.font.poly),
            ResourcesCompat.getFont(this@OrganisationActivity, R.font.adamina)
        )
        FileDataModel.logo = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.logo),
            110,
            110,
            false
        )
        FileDataModel.table = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.attendance_table
            ), 700, 1000, false
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@OrganisationActivity, MainActivity::class.java))
    }

    companion object {
        private const val STORAGE_PERMISSION_TOKEN = 1
    }
}