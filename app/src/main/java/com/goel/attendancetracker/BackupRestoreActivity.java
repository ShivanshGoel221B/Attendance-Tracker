package com.goel.attendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class BackupRestoreActivity extends AppCompatActivity {

    public static FirebaseUser user;
    public static String SIGN_OUT = "com.goel.attendancetracker.signout";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Backup & Restore");
        ((TextView) findViewById(R.id.user_email)).setText(user.getEmail());
    }

    public void createBackup(View view){

    }

    public void createRestore(View view){

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