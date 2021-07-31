package com.goel.attendancetracker.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.goel.attendancetracker.R
import com.goel.attendancetracker.database.Params
import com.goel.attendancetracker.databinding.ActivityBackupRestoreBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupRestoreBinding
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Backup & Restore"
        binding.userEmail.text = user.email
        storageRef = FirebaseStorage.getInstance().getReference("${user.uid}/${Params.DB_NAME}")
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.backupButton.setOnClickListener {
            createBackup()
        }
        binding.restoreBackup.setOnClickListener {
            createRestore()
        }

        binding.logoutButton.setOnClickListener {
            logOut()
        }
    }

    private fun createBackup() {
        AlertDialog.Builder(this@BackupRestoreActivity)
            .setTitle("Confirm Backup")
            .setMessage("Creating the backup will overwrite the existing backup. Do want to continue?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                backupDatabase()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun backupDatabase() {
        val loading = findViewById<ConstraintLayout>(R.id.backup_restore_loading)
        loading.visibility = View.VISIBLE
        val databaseUri = Uri.fromFile(getDatabasePath(Params.DB_NAME))
        val backupTask = storageRef.putFile(databaseUri)
        backupTask.addOnSuccessListener {
            val toast = Toast.makeText(
                this@BackupRestoreActivity,
                "Backed Up Successfully",
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER, 0, 50)
            toast.show()
            loading.visibility = View.INVISIBLE
        }
        backupTask.addOnFailureListener {
            val toast =
                Toast.makeText(this@BackupRestoreActivity, "Back Up Failed", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 50)
            toast.show()
            loading.visibility = View.INVISIBLE
        }
    }

    private fun createRestore() {
        AlertDialog.Builder(this@BackupRestoreActivity)
            .setTitle("Confirm Restore")
            .setMessage("Performing the restore will overwrite the existing data. Do want to continue?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                restoreDatabase()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun restoreDatabase() {
        val loading = findViewById<ConstraintLayout>(R.id.backup_restore_loading)
        loading.visibility = View.VISIBLE
        val databaseFile = File(getDatabasePath(Params.DB_NAME).path)
        storageRef.getFile(databaseFile)
            .addOnSuccessListener {
                val toast = Toast.makeText(
                    this@BackupRestoreActivity,
                    "Data Restored Successfully",
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER, 0, 50)
                toast.show()
                loading.visibility = View.INVISIBLE
            }
            .addOnFailureListener { e: Exception ->
                loading.visibility = View.INVISIBLE
                if (e.message == "Object does not exist at location.") {
                    backupNotFound()
                }
                val toast =
                    Toast.makeText(this@BackupRestoreActivity, "Restore Failed", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 50)
                toast.show()
            }
    }

    private fun backupNotFound() {
        AlertDialog.Builder(this@BackupRestoreActivity)
            .setTitle("Backup Not Found")
            .setMessage("No backup found on this account. Please try with a different account")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut()
            .addOnSuccessListener {
                Toast.makeText(this, R.string.signed_out, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        @JvmStatic
        lateinit var user: FirebaseUser
    }
}