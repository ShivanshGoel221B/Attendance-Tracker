package com.goel.attendancetracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.goel.attendancetracker.R
import com.goel.attendancetracker.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val signInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            data?.let { intent ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    Log.d("Error", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("Error", "Google sign in failed", e)
                    val toast =
                        Toast.makeText(this@SignInActivity, "Sign in failed", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 50)
                    toast.show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.sign_in)
        configureGoogleSignIn()
        setClickListeners()
    }

    private fun configureGoogleSignIn() {
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setClickListeners() {
        binding.signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInResult.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val loading = findViewById<ConstraintLayout>(R.id.loading_sign_in)
        loading.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    BackupRestoreActivity.user = user
                    val toast = Toast.makeText(
                        this@SignInActivity,
                        "Signed in as ${user.displayName}",
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.CENTER, 0, 50)
                    toast.show()
                    loading.visibility = View.GONE
                    startActivity(Intent(this@SignInActivity, BackupRestoreActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Error", "signInWithCredential:failure", task.exception)
                    val toast =
                        Toast.makeText(this@SignInActivity, "Sign in failed", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 50)
                    toast.show()
                    loading.visibility = View.GONE
                }
            }
    }

    public override fun onResume() {
        super.onResume()
        // Check if user is signed in already
        val currentUser = auth.currentUser
        if (currentUser != null) {
            BackupRestoreActivity.user = currentUser
            val toast =
                Toast.makeText(this, "Signed in as ${currentUser.displayName}", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 50)
            toast.show()
            startActivity(Intent(this@SignInActivity, BackupRestoreActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }
}