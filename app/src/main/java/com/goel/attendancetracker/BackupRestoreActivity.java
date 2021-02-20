package com.goel.attendancetracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.goel.attendancetracker.database.Params;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
        setAds();
        // Implementing ads
        MobileAds.initialize(this, initializationStatus -> {
        });
    }

    private void setAds() {
        AdView mAdView_1 = findViewById(R.id.adView);
        AdRequest adRequest_1 = new AdRequest.Builder().build();
        mAdView_1.loadAd(adRequest_1);

        AdView mAdView_2 = findViewById(R.id.adView1);
        AdRequest adRequest_2 = new AdRequest.Builder().build();
        mAdView_2.loadAd(adRequest_2);

        setAdEventListeners(mAdView_1, adRequest_1);
        setAdEventListeners(mAdView_2, adRequest_2);
    }

    private void setAdEventListeners(AdView mAdView, AdRequest adRequest){
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                mAdView.loadAd(adRequest);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                super.onAdClosed();
            }
        });
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
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void backupDatabase(){
        ConstraintLayout loading = findViewById(R.id.backup_restore_loading);
        loading.setVisibility(View.VISIBLE);
        Uri databaseUri = Uri.fromFile(getDatabasePath(Params.DB_NAME));
        UploadTask backupTask = storageRef.putFile(databaseUri);
        backupTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(BackupRestoreActivity.this, "Backed Up Successfully", Toast.LENGTH_LONG).show();
            loading.setVisibility(View.INVISIBLE);
        });
        backupTask.addOnFailureListener(e -> {
            Toast.makeText(BackupRestoreActivity.this, "Back Up Failed", Toast.LENGTH_LONG).show();
            loading.setVisibility(View.INVISIBLE);
        });

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