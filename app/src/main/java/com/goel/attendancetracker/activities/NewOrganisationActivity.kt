package com.goel.attendancetracker.activities

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.attendancetracker.R
import com.goel.attendancetracker.database.DatabaseHandler
import com.goel.attendancetracker.database.Params
import com.goel.attendancetracker.models.OrganisationsModel

class NewOrganisationActivity : AppCompatActivity() {
    private lateinit var databaseHandler: DatabaseHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_organisation)
        databaseHandler = DatabaseHandler(this@NewOrganisationActivity)
    }

    fun submitNewOrganisation(view: View?) {
        val name = findViewById<EditText>(R.id.edit_new_organisation_name)
        val target = findViewById<EditText>(R.id.edit_new_organisation_target)
        if (name.text.toString().isEmpty()) {
            name.error = "Please enter a valid name"
            return
        }
        if (target.text.toString().isEmpty()) {
            target.error = "Please enter a valid number"
            return
        }
        val organisationName = name.text.toString()
        val organisationTarget = target.text.toString().toInt()
        if (organisationName.length > 30) {
            name.error = "Name length should be less than or equal to 30"
            return
        }
        if (organisationTarget < 0 || organisationTarget > 100) {
            target.error = "Please enter a number from 0 to 100"
            return
        }
        val newOrganisation = OrganisationsModel(name = organisationName, target = organisationTarget)
        try {
            databaseHandler.createNewOrganisation(newOrganisation)
            insertOrganisation(newOrganisation)
        } catch (e: SQLiteException) {
            name.error = "Organisation already exists"
            Toast.makeText(this, "Organisation already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertOrganisation(newOrganisation: OrganisationsModel) {
        val values = ContentValues()
        values.put(Params.NAME, newOrganisation.name)
        values.put(Params.TARGET, newOrganisation.target)
        values.put(Params.ATTENDANCE, newOrganisation.attendance)
        databaseHandler.insertOrganisation(values)
        Toast.makeText(this, "Added " + newOrganisation.name + " Successfully", Toast.LENGTH_LONG)
            .show()
        val intent = Intent(applicationContext, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}