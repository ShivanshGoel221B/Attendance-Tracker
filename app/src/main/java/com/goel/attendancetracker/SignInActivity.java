package com.goel.attendancetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Sign In");

        // Configure Google Sign In
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (getIntent().getBooleanExtra(BackupRestoreActivity.SIGN_OUT, false))
            logOut();

        setAds();
    }

    private void setAds() {
        // Implementing ads
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdView mAdView_1 = findViewById(R.id.adView3);
        AdRequest adRequest_1 = new AdRequest.Builder().build();
        mAdView_1.loadAd(adRequest_1);

        AdView mAdView_2 = findViewById(R.id.adView4);
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

    public void signIn(View view) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d("Error", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Error", "Google sign in failed", e);
                Toast toast = Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 50);
                toast.show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        ConstraintLayout loading = findViewById(R.id.loading_sign_in);
        loading.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Success", "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        BackupRestoreActivity.user = user;
                        Toast toast = Toast.makeText(SignInActivity.this, "Signed in as " + user.getDisplayName(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 50);
                        toast.show();
                        loading.setVisibility(View.GONE);
                        startActivity(new Intent(SignInActivity.this, BackupRestoreActivity.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Error", "signInWithCredential:failure", task.getException());
                        Toast toast = Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 50);
                        toast.show();
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    private void logOut(){
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, task -> Toast.makeText(SignInActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null){
            BackupRestoreActivity.user = currentUser;
            Toast toast = Toast.makeText(this, "Signed in as " + currentUser.getDisplayName(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 50);
            toast.show();
            startActivity(new Intent(SignInActivity.this, BackupRestoreActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }
}