package com.goel.attendancetracker.dialogboxes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.attendancetracker.R

class MarkDialogBox(private val listener: MarkAttendanceListener) : AppCompatDialogFragment() {
    private lateinit var markPresent: ImageView
    private lateinit var markAbsent: ImageView
    private var presentCount = 0
    private var absentCount = 0
    private lateinit var presentCounter: TextView
    private lateinit var absentCounter: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.mark_attendance, null)

        setViews(view)

        setClickListeners()

        builder.setView(view)
            .setTitle("Mark Today's Attendance")
            .setNegativeButton("Cancel") { dialog: DialogInterface, _ -> dialog.dismiss() }
            .setPositiveButton("Mark") { _, _ ->
                listener.submitAttendance(
                    presentCount,
                    absentCount
                )
            }
        return builder.create()
    }

    private fun setViews (view: View) {
        markPresent = view.findViewById(R.id.attendance_check)
        markAbsent = view.findViewById(R.id.attendance_cross)
        presentCounter = view.findViewById(R.id.mark_present_counter)
        absentCounter = view.findViewById(R.id.mark_absent_counter)
        listener.setCounters(presentCounter, absentCounter)
    }

    private fun setClickListeners() {
        markPresent.setOnClickListener {
            it.performHapticFeedback(1)
            presentCount++
            setCounters(presentCounter)
        }
        markAbsent.setOnClickListener {
            it.performHapticFeedback(1)
            absentCount++
            setCounters(absentCounter)
        }
    }

    private fun setCounters(textView: TextView) {
        val initialCount = textView.text.toString().toInt()
        textView.text = (initialCount + 1).toString()
    }

    interface MarkAttendanceListener {
        fun submitAttendance(presentCount: Int, absentCount: Int)
        fun setCounters(presentCounter: TextView, absentCounter: TextView)
    }
}