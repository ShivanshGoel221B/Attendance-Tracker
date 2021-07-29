package com.goel.attendancetracker.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.goel.attendancetracker.R
import com.goel.attendancetracker.database.Params
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*

class BackupRestoreActivity : AppCompatActivity() {
    private lateinit var storageRef: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)
        supportActionBar?.title = "Backup & Restore"
        (findViewById<View>(R.id.user_email) as TextView).text =
            user!!.email
        storageRef = FirebaseStorage.getInstance().getReference(user!!.uid + "/" + Params.DB_NAME)
    }

    fun createBackup(view: View?) {
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

    fun createRestore(view: View?) {
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

    fun logOut(view: View?) {
        FirebaseAuth.getInstance().signOut()
        startActivity(
            Intent(this@BackupRestoreActivity, SignInActivity::class.java).putExtra(
                SIGN_OUT, true
            )
        )
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@BackupRestoreActivity, MainActivity::class.java))
        finish()
    }

    companion object {
        @JvmField
        var user: FirebaseUser? = null
        const val SIGN_OUT = "com.goel.attendancetracker.signout"
    }
}