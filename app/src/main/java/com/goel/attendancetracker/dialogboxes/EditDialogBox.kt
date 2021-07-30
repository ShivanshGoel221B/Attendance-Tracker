package com.goel.attendancetracker.dialogboxes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.attendancetracker.R

class EditDialogBox(private val listener: EditDialogListener) : AppCompatDialogFragment() {
    private lateinit var newNameText: EditText
    private lateinit var newTargetText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_dialog_box, null)
        newNameText = view.findViewById(R.id.edit_name)
        newTargetText = view.findViewById(R.id.edit_target)
        builder.setView(view)
            .setTitle("Edit")
            .setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setPositiveButton("Save") { _, _ ->
                listener.submitDetails(
                    newNameText,
                    newTargetText
                )
            }
        newNameText.setText(name)
        newTargetText.setText(target.toString())
        return builder.create()
    }

    interface EditDialogListener {
        fun submitDetails(newNameText: EditText, newTargetText: EditText)
    }

    companion object {
        var name: String? = null
        var target = 0
    }
}