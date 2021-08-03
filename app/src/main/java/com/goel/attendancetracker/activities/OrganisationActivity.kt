package com.goel.attendancetracker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.goel.attendancetracker.R
import com.goel.attendancetracker.adapters.ClassesAdapter
import com.goel.attendancetracker.databinding.ActivityOrganisationBinding
import com.goel.attendancetracker.dialogboxes.AddDialogBox
import com.goel.attendancetracker.dialogboxes.AddDialogBox.AddDialogListener
import com.goel.attendancetracker.dialogboxes.EditDialogBox
import com.goel.attendancetracker.dialogboxes.EditDialogBox.EditDialogListener
import com.goel.attendancetracker.dialogboxes.MarkDialogBox
import com.goel.attendancetracker.dialogboxes.MarkDialogBox.MarkAttendanceListener
import com.goel.attendancetracker.downloadmanager.ClassDownloadManager
import com.goel.attendancetracker.downloadmanager.FileDataModel
import com.goel.attendancetracker.models.ClassesModel
import com.goel.attendancetracker.utils.Constants
import com.goel.attendancetracker.utils.database.DatabaseHandler
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.*

class OrganisationActivity : AppCompatActivity(), EditDialogListener, AddDialogListener,
    MarkAttendanceListener, ClassesAdapter.OnItemClickListener {

    private lateinit var binding: ActivityOrganisationBinding
    private lateinit var organisationName: String
    private lateinit var organisationId: String
    private lateinit var databaseHandler: DatabaseHandler
    private var overallAttendance = 0
    private var overallRequiredAttendance = 0
    private lateinit var classAdapter: ClassesAdapter
    private lateinit var classList: ArrayList<ClassesModel>
    private var focusedClass: ClassesModel? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrganisationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.empty.visibility = View.GONE
        organisationId = Constants.OPEN_ORG!!
        classList = ArrayList()
        databaseHandler = DatabaseHandler(this)
        initializeOrganisation()
        supportActionBar?.title = organisationName
        setAdapter()
        getClassList()
        refreshProgress()
        binding.targetValue.text = "$overallRequiredAttendance%"
    }

    //-- Context Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.organisation_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_new_class -> addNewClass()
            R.id.delete_all_classes -> deleteAllClasses()
        }
        return super.onOptionsItemSelected(item)
    }

    //===========================================
    private fun setAdapter() {
        classAdapter = ClassesAdapter(classList, this, this)
        binding.classGrid.adapter = classAdapter
        val layoutManager = GridLayoutManager(this, 2)
        binding.classGrid.layoutManager = layoutManager
    }

    private fun getClassList() {
        val readable = databaseHandler.readableDatabase
        val getCommand = "SELECT * FROM \"$organisationName\""
        @SuppressLint("Recycle") val cursor = readable.rawQuery(getCommand, null)
        if (cursor.moveToFirst()) {
            do {
                val model = ClassesModel()
                model.id = cursor.getInt(0)
                model.name = cursor.getString(1)
                model.attendance = cursor.getInt(2)
                model.target = cursor.getInt(3)
                model.classHistory = cursor.getString(4)
                model.setClassCounter()
                classList.add(model)
                classAdapter.notifyItemInserted(classList.indexOf(model))
            } while (cursor.moveToNext())
        }
        checkForEmpty()
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
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_all_prompt))
            .setPositiveButton(R.string.confirm) { _, _ ->
                for (model in classList) databaseHandler.deleteClass(
                    organisationName,
                    model.id.toString()
                )
                classList.clear()
                classAdapter.notifyDataSetChanged()
                checkForEmpty()
                Toast.makeText(this, R.string.deleted_all_classes, Toast.LENGTH_LONG)
                    .show()
                refreshProgress()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun checkForEmpty() {
        binding.empty.visibility = if (classList.isEmpty())
            View.VISIBLE
        else
            View.GONE
    }

    override fun addSubmitDetails(newNameText: EditText, newTargetText: EditText) {
        val nameValidity = Constants.getNameValidity(newNameText.text.toString())
        val targetValidity = Constants.getTargetValidity(newTargetText.text.toString())

        if (nameValidity.containsKey(false)) {
            Toast.makeText(this, nameValidity[false], Toast.LENGTH_SHORT).show()
            return
        }
        if (targetValidity.containsKey(false)) {
            Toast.makeText(this, targetValidity[false], Toast.LENGTH_SHORT).show()
            return
        }
        val className: String
        val classTarget = try {
            className = nameValidity[true]!!
            targetValidity[true]!!.toInt()
        } catch (e: NullPointerException) {
            return
        }
        val values = ContentValues()
        val model = ClassesModel(name = className, target = classTarget)
        values.put(Constants.NAME, model.name)
        values.put(Constants.TARGET, model.target)
        values.put(Constants.ATTENDANCE, model.attendance)
        values.put(Constants.HISTORY, model.classHistory)
        val classId = databaseHandler.addNewClass(organisationName, values)
        model.id = classId
        classList.add(model)
        classAdapter.notifyItemInserted(classList.indexOf(model))
        refreshProgress()
        checkForEmpty()
    }

    //========== DOWNLOAD ATTENDANCE ============//
    private fun hasStoragePermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_TOKEN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                downloadClassAttendance()
            else
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadClassAttendance() {
        if (hasStoragePermission()) {
            val classDownloadManager = ClassDownloadManager(focusedClass!!, organisationName)
            setFileModelData()
            when (classDownloadManager.downloadAttendance()) {
                ClassDownloadManager.DOWNLOAD_FAILED -> Toast.makeText(
                    this,
                    R.string.download_failed,
                    Toast.LENGTH_SHORT
                ).show()
                ClassDownloadManager.DOWNLOAD_SUCCESSFUL -> Toast.makeText(
                    this,
                    getString(R.string.file_saved, classDownloadManager.filePath),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
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
        EditDialogBox.name = focusedClass!!.name
        EditDialogBox.target = focusedClass!!.target
    }

    private fun deleteClass(position: Int) {
        val classId = classList[position].id.toString()
        val className = classList[position].name
        databaseHandler.deleteClass(organisationName, classId)
        classAdapter.notifyItemRemoved(position)
        Toast.makeText(
            this,
            getString(R.string.deleted, className),
            Toast.LENGTH_LONG
        ).show()
        classList.removeAt(position)
        refreshProgress()
        checkForEmpty()
    }

    //==================================================================//
    private fun initializeOrganisation() {
        val requiredOverallProgress = binding.overallRequiredAttendance
        val getCommand = "SELECT * FROM ${Constants.ORGANISATIONS} WHERE s_no=$organisationId"
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
            Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
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
        Toast.makeText(this, R.string.attendance_marked, Toast.LENGTH_SHORT).show()
        focusedClass = null
    }

    override fun submitDetails(newNameText: EditText, newTargetText: EditText) {
        val nameValidity = Constants.getNameValidity(newNameText.text.toString())
        val targetValidity = Constants.getTargetValidity(newTargetText.text.toString())

        if (nameValidity.containsKey(false)) {
            Toast.makeText(this, nameValidity[false], Toast.LENGTH_SHORT).show()
            return
        }
        if (targetValidity.containsKey(false)) {
            Toast.makeText(this, targetValidity[false], Toast.LENGTH_SHORT).show()
            return
        }
        val className: String
        val classTarget = try {
            className = nameValidity[true]!!
            targetValidity[true]!!.toInt()
        } catch (e: NullPointerException) {
            return
        }
        val values = ContentValues()
        values.put(Constants.NAME, className)
        values.put(Constants.TARGET, classTarget)
        databaseHandler.updateClass(organisationName, values, focusedClass!!.id.toString())
        focusedClass!!.name = className
        focusedClass!!.target = classTarget
        classAdapter.notifyItemChanged(classList.indexOf(focusedClass))
        Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show()
        focusedClass = null
    }

    @SuppressLint("SetTextI18n")
    private fun refreshProgress() {
        val overallProgress = binding.overallAttendance
        val flag = binding.targetFlag
        overallAttendance = databaseHandler.refreshOverAttendance(organisationName)
        binding.overallPercentageCounter.text = "$overallAttendance%"
        overallProgress.progress = 0
        when {
            overallAttendance >= overallRequiredAttendance -> {
                overallProgress.progressDrawable =
                    ContextCompat.getDrawable(this, R.drawable.attendance_progress)
                flag.imageTintList = ColorStateList.valueOf(Color.parseColor("#00CF60"))
            }
            overallAttendance > overallRequiredAttendance * 0.75f -> {
                overallProgress.progressDrawable = ContextCompat.getDrawable(
                    this,
                    R.drawable.attendance_progress_low
                )
                flag.imageTintList = ColorStateList.valueOf(Color.parseColor("#F56600"))
            }
            else -> {
                overallProgress.progressDrawable = ContextCompat.getDrawable(
                    this,
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
            ResourcesCompat.getFont(this, R.font.halant),
            ResourcesCompat.getFont(this, R.font.halant_medium),
            ResourcesCompat.getFont(this, R.font.halant_semibold),
            ResourcesCompat.getFont(this, R.font.poly),
            ResourcesCompat.getFont(this, R.font.adamina)
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

    override fun onHistoryClick(position: Int) {
        CalendarActivity.model = classList[position]
        CalendarActivity.organisationName = organisationName
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

    override fun onDeleteClick(position: Int) {
        val name = classList[position].name
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_class_prompt, name))
            .setPositiveButton(R.string.confirm) { _, _ ->
                deleteClass(position)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {
        private const val STORAGE_PERMISSION_TOKEN = 1
    }
}