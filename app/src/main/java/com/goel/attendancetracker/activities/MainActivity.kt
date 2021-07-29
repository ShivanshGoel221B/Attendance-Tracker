package com.goel.attendancetracker.activities

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.attendancetracker.R
import com.goel.attendancetracker.database.DatabaseHandler
import com.goel.attendancetracker.database.Params
import com.goel.attendancetracker.dialogboxes.EditDialogBox
import com.goel.attendancetracker.dialogboxes.EditDialogBox.EditDialogListener
import com.goel.attendancetracker.organisations.OrganisationsAdapter
import com.goel.attendancetracker.organisations.OrganisationsModel
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), EditDialogListener {
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var organisationContainer: RecyclerView
    private lateinit var organisationsAdapter: OrganisationsAdapter
    private lateinit var organisationList: ArrayList<OrganisationsModel>
    private var focusedOrganisation: OrganisationsModel? = null

    private val APP_URL = "https://play.google.com/store/apps/details?id=com.goel.attendancetracker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        organisationContainer = findViewById(R.id.organisations_container)
        organisationList = ArrayList()
        databaseHandler = DatabaseHandler(this@MainActivity)
        setOrganisationAdapter()
        getOrganisationList()
        setClickListeners()
        Params.OPEN_ORG = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_backup_restore -> startActivity(
                Intent(
                    this@MainActivity,
                    SignInActivity::class.java
                )
            )
            R.id.menu_share -> {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, APP_URL)
                shareIntent.type = "text/plain"
                Intent.createChooser(shareIntent, "Share via")
                startActivity(shareIntent)
            }
            R.id.menu_rate_us -> {
                val appUri = Uri.parse(APP_URL)
                val rateIntent = Intent(Intent.ACTION_VIEW, appUri)
                startActivity(rateIntent)
            }
        }
        return true
    }

    private fun setOrganisationAdapter() {
        organisationsAdapter = OrganisationsAdapter(organisationList, this)
        organisationContainer.adapter = organisationsAdapter
        val organisationLayout = LinearLayoutManager(this)
        organisationContainer.layoutManager = organisationLayout
    }

    private fun getOrganisationList() {
        val organisationsRef = databaseHandler.readableDatabase
        val getCommand = "SELECT * FROM " + Params.ORGANISATIONS
        val cursor = organisationsRef.rawQuery(getCommand, null)
        if (cursor.moveToFirst()) {
            do {
                val organisation = OrganisationsModel()
                organisation.id = cursor.getInt(0)
                organisation.organisationName = cursor.getString(1)
                organisation.organisationAttendancePercentage = cursor.getInt(2)
                organisation.requiredAttendance = cursor.getInt(3)
                organisationList.add(organisation)
                organisationsAdapter.notifyItemInserted(organisationsAdapter.itemCount - 1)
            } while (cursor.moveToNext())
        }
        cursor.close()
        organisationsRef.close()
    }

    private fun setClickListeners() {
        organisationsAdapter.setOnItemClickListener(object :
            OrganisationsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Params.OPEN_ORG = organisationList[position].id.toString()
                openOrganisation()
            }

            override fun onEditClick(position: Int) {
                focusedOrganisation = organisationList[position]
                editOrganisation()
            }

            override fun onDeleteClick(position: Int) {
                deleteOrganisation(position)
            }
        })
    }

    private fun openOrganisation() {
        val intent = Intent(applicationContext, OrganisationActivity::class.java)
        startActivity(intent)
    }

    private fun editOrganisation() {
        val editDialogBox = EditDialogBox()
        editDialogBox.show(supportFragmentManager, "edit dialog")
        EditDialogBox.name = focusedOrganisation!!.organisationName
        EditDialogBox.target = focusedOrganisation!!.requiredAttendance
    }

    private fun deleteOrganisation(position: Int) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage("Are you sure you want to delete " + organisationList[position].organisationName + " ?")
            .setPositiveButton("Yes") { _, _ ->
                organisationsAdapter.notifyItemRemoved(position)
                databaseHandler.deleteOrganisation(
                    organisationList[position].id.toString(),
                    organisationList[position].organisationName
                )
                Toast.makeText(
                    this@MainActivity,
                    "Deleted " + organisationList[position].organisationName,
                    Toast.LENGTH_LONG
                ).show()
                organisationList.removeAt(position)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun addOrganisationButton(view: View?) {
        val intent = Intent(this@MainActivity, NewOrganisationActivity::class.java)
        startActivity(intent)
    }

    override fun submitDetails(newNameText: EditText, newTargetText: EditText) {
        if (isDataValid(newNameText, newTargetText)) {
            val values = ContentValues()
            values.put("name", newNameText.text.toString())
            values.put("target", newTargetText.text.toString().toInt())
            databaseHandler.updateOrganisation(values, focusedOrganisation!!.organisationName)
            if (newNameText.text.toString() != focusedOrganisation!!.organisationName) databaseHandler.renameOrganisationTable(
                focusedOrganisation!!.organisationName, newNameText.text.toString()
            )
            focusedOrganisation?.organisationName = newNameText.text.toString()
            focusedOrganisation?.requiredAttendance = newTargetText.text.toString().toInt()
            organisationsAdapter.notifyItemChanged(organisationList.indexOf(focusedOrganisation!!))
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show()
            focusedOrganisation = null
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
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        exitProcess(0)
    }
}