package com.goel.attendancetracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goel.attendancetracker.database.DatabaseHandler;
import com.goel.attendancetracker.database.Params;
import com.goel.attendancetracker.organisations.OrganisationsAdapter;
import com.goel.attendancetracker.organisations.OrganisationsModel;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements EditDialogBox.EditDialogListener {
    private DatabaseHandler databaseHandler;
    private RecyclerView organisationContainer;
    private OrganisationsAdapter organisationsAdapter;
    private ArrayList<OrganisationsModel> organisationList;
    private OrganisationsModel focusedOrganisation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        organisationContainer = findViewById(R.id.organisations_container);
        organisationList = new ArrayList<>();
        databaseHandler = new DatabaseHandler(MainActivity.this);
        setOrganisationAdapter();
        getOrganisationList();
        setClickListeners();
        Params.OPEN_ORG = null;
    }


    private void setOrganisationAdapter() {
        organisationsAdapter = new OrganisationsAdapter(organisationList, this);
        organisationContainer.setAdapter(organisationsAdapter);

        LinearLayoutManager organisationLayout = new LinearLayoutManager(this);
        organisationContainer.setLayoutManager(organisationLayout);
    }

    private void getOrganisationList() {
        SQLiteDatabase organisationsRef =  databaseHandler.getReadableDatabase();
        String getCommand = "SELECT * FROM " + Params.ORGANISATIONS;
        Cursor cursor = organisationsRef.rawQuery(getCommand, null);

        if (cursor.moveToFirst())
        {
            do {
                OrganisationsModel organisation = new OrganisationsModel();
                organisation.setId(cursor.getInt(0));
                organisation.setOrganisationName(cursor.getString(1));
                organisation.setOrganisationAttendancePercentage(cursor.getInt(2));
                organisation.setRequiredAttendance(cursor.getInt(3));
                organisationList.add(organisation);
                organisationsAdapter.notifyItemInserted(organisationsAdapter.getItemCount()-1);
            }while (cursor.moveToNext());
        }
        cursor.close();
        organisationsRef.close();
    }

    private void setClickListeners()
    {
        organisationsAdapter.setOnItemClickListener(new OrganisationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Params.OPEN_ORG = String.valueOf(organisationList.get(position).getId());
                openOrganisation();
            }

            @Override
            public void onEditClick(int position) {
                focusedOrganisation = organisationList.get(position);
                editOrganisation();
            }

            @Override
            public void onDeleteClick(int position) {
                deleteOrganisation(position);
            }

        });
    }


    private void openOrganisation() {
        Intent intent = new Intent(getApplicationContext(), OrganisationActivity.class);
        startActivity(intent);
    }

    private void editOrganisation() {
        EditDialogBox editDialogBox = new EditDialogBox();
        editDialogBox.show(getSupportFragmentManager(), "edit dialog");
        EditDialogBox.name = focusedOrganisation.getOrganisationName();
        EditDialogBox.target = focusedOrganisation.getRequiredAttendance();
    }

    private void deleteOrganisation(int position) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Are you sure you want to delete " + organisationList.get(position).getOrganisationName() + " ?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    organisationsAdapter.notifyItemRemoved(position);
                    databaseHandler.deleteOrganisation(String.valueOf(organisationList.get(position).getId()), organisationList.get(position).getOrganisationName());
                    Toast.makeText(MainActivity.this, "Deleted " + organisationList.get(position).getOrganisationName(), Toast.LENGTH_LONG).show();
                    organisationList.remove(position);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void addOrganisationButton(View view) {
        Intent intent = new Intent(MainActivity.this, NewOrganisationActivity.class);
        startActivity(intent);
    }

    @Override
    public void submitDetails(EditText newNameText, EditText newTargetText) {
        if (isDataValid(newNameText, newTargetText))
        {
            ContentValues values = new ContentValues();
            values.put("name", newNameText.getText().toString());
            values.put("target", Integer.parseInt(newTargetText.getText().toString()));
            databaseHandler.updateOrganisation(values, String.valueOf(focusedOrganisation.getId()));
            focusedOrganisation.setOrganisationName(newNameText.getText().toString());
            focusedOrganisation.setRequiredAttendance(Integer.parseInt(newTargetText.getText().toString()));
            organisationsAdapter.notifyItemChanged(organisationList.indexOf(focusedOrganisation));
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_LONG).show();
            focusedOrganisation = null;
        }
    }

    private boolean isDataValid(EditText newNameText, EditText newTargetText) {
        String name;
        int target;
        try {
            name = newNameText.getText().toString();
        } catch (Exception e) {
            name = "";
        }
        try {
            target = Integer.parseInt(newTargetText.getText().toString());
        } catch (NumberFormatException e) {
            target = 101;
        }
        if (name.isEmpty()) {
            newNameText.setError("Please Enter a valid name");
            return false;
        }
        if (name.length()>30){
            newNameText.setError("The length of the name should be less than or equal to 30");
            return false;
        }
        if (target>100 || target<0){
            newTargetText.setError("Enter a valid number from 0 to 100");
        }
        return true;
    }
}