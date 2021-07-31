package com.goel.attendancetracker.activities

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.attendancetracker.R
import com.goel.attendancetracker.adapters.OrganisationsAdapter
import com.goel.attendancetracker.databinding.ActivityMainBinding
import com.goel.attendancetracker.dialogboxes.EditDialogBox
import com.goel.attendancetracker.dialogboxes.EditDialogBox.EditDialogListener
import com.goel.attendancetracker.models.OrganisationsModel
import com.goel.attendancetracker.utils.Constants
import com.goel.attendancetracker.utils.Constants.APP_URL
import com.goel.attendancetracker.utils.database.DatabaseHandler
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), EditDialogListener, OrganisationsAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var organisationsAdapter: OrganisationsAdapter
    private lateinit var organisationList: ArrayList<OrganisationsModel>
    private var focusedOrganisation: OrganisationsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        organisationList = ArrayList()
        databaseHandler = DatabaseHandler(this@MainActivity)
        setOrganisationAdapter()
        getOrganisationList()
        binding.newOrganisationButton.setOnClickListener { addOrganisation() }
        Constants.OPEN_ORG = null
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
        binding.organisationsContainer.adapter = organisationsAdapter
        val organisationLayout = LinearLayoutManager(this)
        binding.organisationsContainer.layoutManager = organisationLayout
    }

    private fun getOrganisationList() {
        val organisationsRef = databaseHandler.readableDatabase
        val getCommand = "SELECT * FROM " + Constants.ORGANISATIONS
        val cursor = organisationsRef.rawQuery(getCommand, null)
        if (cursor.moveToFirst()) {
            do {
                val organisation = OrganisationsModel()
                organisation.id = cursor.getInt(0)
                organisation.name = cursor.getString(1)
                organisation.attendance = cursor.getInt(2)
                organisation.target = cursor.getInt(3)
                organisationList.add(organisation)
                organisationsAdapter.notifyItemInserted(organisationsAdapter.itemCount - 1)
            } while (cursor.moveToNext())
        }
        cursor.close()
        organisationsRef.close()
    }

    private fun openOrganisation() {
        val intent = Intent(applicationContext, OrganisationActivity::class.java)
        startActivity(intent)
    }

    private fun editOrganisation() {
        val editDialogBox = EditDialogBox(this)
        editDialogBox.show(supportFragmentManager, "edit dialog")
        EditDialogBox.name = focusedOrganisation!!.name
        EditDialogBox.target = focusedOrganisation!!.target
    }

    private fun deleteOrganisation(position: Int) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage("Are you sure you want to delete " + organisationList[position].name + " ?")
            .setPositiveButton("Yes") { _, _ ->
                organisationsAdapter.notifyItemRemoved(position)
                databaseHandler.deleteOrganisation(
                    organisationList[position].id.toString(),
                    organisationList[position].name
                )
                Toast.makeText(
                    this@MainActivity,
                    "Deleted " + organisationList[position].name,
                    Toast.LENGTH_LONG
                ).show()
                organisationList.removeAt(position)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun addOrganisation() {
        val intent = Intent(this@MainActivity, NewOrganisationActivity::class.java)
        startActivity(intent)
    }

    override fun submitDetails(newNameText: EditText, newTargetText: EditText) {
        if (isDataValid(newNameText, newTargetText)) {
            val values = ContentValues()
            values.put("name", newNameText.text.toString())
            values.put("target", newTargetText.text.toString().toInt())
            databaseHandler.updateOrganisation(values, focusedOrganisation!!.name)
            if (newNameText.text.toString() != focusedOrganisation!!.name) databaseHandler.renameOrganisationTable(
                focusedOrganisation!!.name, newNameText.text.toString()
            )
            focusedOrganisation?.name = newNameText.text.toString()
            focusedOrganisation?.target = newTargetText.text.toString().toInt()
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

    override fun onItemClick(position: Int) {
        Constants.OPEN_ORG = organisationList[position].id.toString()
        openOrganisation()
    }

    override fun onEditClick(position: Int) {
        focusedOrganisation = organisationList[position]
        editOrganisation()
    }

    override fun onDeleteClick(position: Int) {
        deleteOrganisation(position)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        exitProcess(0)
    }
}