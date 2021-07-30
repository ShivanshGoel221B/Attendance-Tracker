package com.goel.attendancetracker.dialogboxes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.attendancetracker.R

class AddDialogBox(private val listener: AddDialogListener) : AppCompatDialogFragment() {
    private lateinit var newNameText: EditText
    private lateinit var newTargetText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_dialog_box, null)
        newNameText = view.findViewById(R.id.edit_name)
        newTargetText = view.findViewById(R.id.edit_target)
        builder.setView(view)
            .setTitle("Add New Class")
            .setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setPositiveButton("Save") { _, _ ->
                listener.addSubmitDetails(newNameText, newTargetText)
            }
        newTargetText.setText(overallAttendance.toString())
        return builder.create()
    }

    interface AddDialogListener {
        fun addSubmitDetails(newNameText: EditText, newTargetText: EditText)
    }

    companion object {
        var overallAttendance = 100
    }
}