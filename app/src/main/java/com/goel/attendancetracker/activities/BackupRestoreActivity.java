package com.goel.attendancetracker.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goel.attendancetracker.R;
import com.goel.attendancetracker.database.Params;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Objects;

public class BackupRestoreActivity extends AppCompatActivity {

    public static FirebaseUser user;
    public static final String SIGN_OUT = "com.goel.attendancetracker.signout";
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Backup & Restore");
        ((TextView) findViewById(R.id.user_email)).setText(user.getEmail());

        storageRef = FirebaseStorage.getInstance().getReference(user.getUid() + "/" + Params.DB_NAME);
    }

    public void createBackup(View view){
        new AlertDialog.Builder(BackupRestoreActivity.this)
                .setTitle("Confirm Backup")
                .setMessage("Creating the backup will overwrite the existing backup. Do want to continue?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    dialog.dismiss();
                    backupDatabase();
                })

                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void backupDatabase(){
        ConstraintLayout loading = findViewById(R.id.backup_restore_loading);
        loading.setVisibility(View.VISIBLE);
        Uri databaseUri = Uri.fromFile(getDatabasePath(Params.DB_NAME));
        UploadTask backupTask = storageRef.putFile(databaseUri);
        backupTask.addOnSuccessListener(taskSnapshot -> {
            Toast toast = Toast.makeText(BackupRestoreActivity.this, "Backed Up Successfully", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 50);
            toast.show();
            loading.setVisibility(View.INVISIBLE);
        });
        backupTask.addOnFailureListener(e -> {
            Toast toast = Toast.makeText(BackupRestoreActivity.this, "Back Up Failed", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 50);
            toast.show();
            loading.setVisibility(View.INVISIBLE);
        });

    }

    public void createRestore(View view){
        new AlertDialog.Builder(BackupRestoreActivity.this)
                .setTitle("Confirm Restore")
                .setMessage("Performing the restore will overwrite the existing data. Do want to continue?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    dialog.dismiss();
                    restoreDatabase();
                })

                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void restoreDatabase() {
        ConstraintLayout loading = findViewById(R.id.backup_restore_loading);
        loading.setVisibility(View.VISIBLE);
        File databaseFile = new File(getDatabasePath(Params.DB_NAME).getPath());

        storageRef.getFile(databaseFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast toast = Toast.makeText(BackupRestoreActivity.this, "Data Restored Successfully", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 50);
                    toast.show();
                            loading.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    loading.setVisibility(View.INVISIBLE);
                    if (e.getMessage().equals("Object does not exist at location.")){
                        backupNotFound();
                    }
                    Toast toast = Toast.makeText(BackupRestoreActivity.this, "Restore Failed", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 50);
                    toast.show();
                });
    }

    private void backupNotFound() {
        new AlertDialog.Builder(BackupRestoreActivity.this)
                .setTitle("Backup Not Found")
                .setMessage("No backup found on this account. Please try with a different account")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void logOut(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(BackupRestoreActivity.this, SignInActivity.class).putExtra(SIGN_OUT, true));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(BackupRestoreActivity.this, MainActivity.class));
        finish();
    }
}