package com.goel.attendancetracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goel.attendancetracker.database.DatabaseHandler;
import com.goel.attendancetracker.database.OrganisationsDataModel;
import com.goel.attendancetracker.database.Params;

public class NewOrganisationActivity extends AppCompatActivity {

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_organisation);

        databaseHandler = new DatabaseHandler(NewOrganisationActivity.this);
    }


    public void submitNewOrganisation(View view)
    {
        EditText name = findViewById(R.id.edit_new_organisation_name);
        EditText target = findViewById(R.id.edit_new_organisation_target);

        if (name.getText().toString().isEmpty())
        {
            name.setError("Please enter a valid name");
            return;
        }

        if (target.getText().toString().isEmpty())
        {
            target.setError("Please enter a valid number");
            return;
        }

        String organisationName = name.getText().toString();
        int organisationTarget = Integer.parseInt(target.getText().toString());

        if (organisationName.length() > 30)
        {
            name.setError("Name length should be less than or equal to 30");
            return;
        }

        if (organisationTarget < 0 || organisationTarget > 100)
        {
            target.setError("Please enter a number from 0 to 100");
            return;
        }

        OrganisationsDataModel newOrganisation = new OrganisationsDataModel(organisationName, organisationTarget);

        try {
            databaseHandler.createNewOrganisation(newOrganisation);
            insertOrganisation(newOrganisation);
        } catch (SQLiteException e) {
            name.setError("Organisation already exists");
            Toast.makeText(this, "Organisation already exists", Toast.LENGTH_SHORT).show();
        }
    }


    private void insertOrganisation(OrganisationsDataModel newOrganisation) {

        ContentValues values = new ContentValues();
        values.put(Params.NAME, newOrganisation.getName());
        values.put(Params.TARGET, newOrganisation.getTarget());
        values.put(Params.ATTENDANCE, newOrganisation.getAttendance());
        databaseHandler.insertOrganisation(values);

        Toast.makeText(this, "Added " + newOrganisation.getName() + " Successfully", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }

}