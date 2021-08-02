package com.goel.attendancetracker.activities

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.attendancetracker.databinding.ActivityNewOrganisationBinding
import com.goel.attendancetracker.models.OrganisationsModel
import com.goel.attendancetracker.utils.Constants
import com.goel.attendancetracker.utils.database.DatabaseHandler

class NewOrganisationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewOrganisationBinding
    private lateinit var databaseHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewOrganisationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseHandler = DatabaseHandler(this@NewOrganisationActivity)
        binding.newOrganisationSubmit.setOnClickListener { submitNewOrganisation() }
    }

    private fun submitNewOrganisation() {
        val name = binding.editNewOrganisationName
        val target = binding.editNewOrganisationTarget
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
        values.put(Constants.NAME, newOrganisation.name)
        values.put(Constants.TARGET, newOrganisation.target)
        values.put(Constants.ATTENDANCE, newOrganisation.attendance)
        databaseHandler.insertOrganisation(values)
        Toast.makeText(this, "Added " + newOrganisation.name + " Successfully", Toast.LENGTH_LONG)
            .show()
        val intent = Intent(applicationContext, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}