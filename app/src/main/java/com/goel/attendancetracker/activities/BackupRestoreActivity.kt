package com.goel.attendancetracker.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.attendancetracker.R
import com.goel.attendancetracker.databinding.ActivityBackupRestoreBinding
import com.goel.attendancetracker.utils.Constants
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
        supportActionBar?.title = getString(R.string.backup_and_restore)
        binding.userEmail.text = user.email
        storageRef = FirebaseStorage.getInstance().getReference("${user.uid}/${Constants.DB_NAME}")
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
            .setTitle(R.string.confirm_backup)
            .setMessage(R.string.backup_prompt)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                dialog.dismiss()
                backupDatabase()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun backupDatabase() {
        val loading = binding.backupRestoreLoading
        loading.visibility = View.VISIBLE
        val databaseUri = Uri.fromFile(getDatabasePath(Constants.DB_NAME))
        val backupTask = storageRef.putFile(databaseUri)
        backupTask.addOnSuccessListener {
            val toast = Toast.makeText(
                this@BackupRestoreActivity,
                R.string.backup_successful,
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER, 0, 50)
            toast.show()
            loading.visibility = View.INVISIBLE
        }
        backupTask.addOnFailureListener {
            val toast =
                Toast.makeText(this@BackupRestoreActivity, R.string.backup_failed, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 50)
            toast.show()
            loading.visibility = View.INVISIBLE
        }
    }

    private fun createRestore() {
        AlertDialog.Builder(this@BackupRestoreActivity)
            .setTitle(R.string.confirm_restore)
            .setMessage(R.string.restore_prompt)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                dialog.dismiss()
                restoreDatabase()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun restoreDatabase() {
        val loading = binding.backupRestoreLoading
        loading.visibility = View.VISIBLE
        val databaseFile = File(getDatabasePath(Constants.DB_NAME).path)
        storageRef.getFile(databaseFile)
            .addOnSuccessListener {
                val toast = Toast.makeText(
                    this@BackupRestoreActivity,
                    R.string.restore_successful,
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER, 0, 50)
                toast.show()
                loading.visibility = View.INVISIBLE
            }
            .addOnFailureListener { e: Exception ->
                loading.visibility = View.INVISIBLE
                if (e.message == getString(R.string.backup_not_found_exception)) {
                    backupNotFound()
                }
                val toast =
                    Toast.makeText(this@BackupRestoreActivity, R.string.restore_failed, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 50)
                toast.show()
            }
    }

    private fun backupNotFound() {
        AlertDialog.Builder(this@BackupRestoreActivity)
            .setTitle(R.string.backup_not_found_title)
            .setMessage(R.string.backup_not_found_message)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
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