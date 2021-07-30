package com.goel.attendancetracker.dialogboxes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.attendancetracker.R
import java.util.*

class EditAttendanceDialog : AppCompatDialogFragment() {
    private lateinit var presentCounter: TextView
    private lateinit var absentCounter: TextView
    private lateinit var presentIncrease: Button
    private lateinit var presentDecrease: Button
    private lateinit var absentIncrease: Button
    private lateinit var absentDecrease: Button
    private lateinit var submitListener: SubmitNewAttendance

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.edit_attendance_dialog, null)
        setViews(view)
        setListeners()
        builder.setView(view)
            .setTitle("Edit Attendance")
            .setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setPositiveButton("Update") { _, _ ->
                submitListener.updateAttendance(presentCount, absentCount)
            }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        submitListener = try {
            context as SubmitNewAttendance
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must Implement MarkAttendanceActions")
        }
    }

    private fun setViews(view: View) {
        presentCounter = view.findViewById(R.id.dialog_present_counter)
        absentCounter = view.findViewById(R.id.dialog_absent_counter)
        presentIncrease = view.findViewById(R.id.present_increment)
        presentDecrease = view.findViewById(R.id.present_decrement)
        absentIncrease = view.findViewById(R.id.absent_increment)
        absentDecrease = view.findViewById(R.id.absent_decrement)
        updateCounters()
    }

    private fun setListeners() {
        presentIncrease.setOnClickListener {
            it.performHapticFeedback(1)
            presentCount++
            updateCounters()
        }
        presentDecrease.setOnClickListener {
            it.performHapticFeedback(1)
            if (presentCount == 0) return@setOnClickListener
            presentCount--
            updateCounters()
        }
        absentIncrease.setOnClickListener {
            it.performHapticFeedback(1)
            absentCount++
            updateCounters()
        }
        absentDecrease.setOnClickListener {
            it.performHapticFeedback(1)
            if (absentCount == 0) return@setOnClickListener
            absentCount--
            updateCounters()
        }
    }

    private fun updateCounters() {
        presentCounter.text = presentCount.toString()
        absentCounter.text = absentCount.toString()
    }

    interface SubmitNewAttendance {
        fun updateAttendance(newPresent: Int, newAbsent: Int)
    }

    companion object {
        var presentCount = 0
        var absentCount = 0
    }
}