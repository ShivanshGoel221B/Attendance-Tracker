package com.goel.attendancetracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class MarkDialogBox extends AppCompatDialogFragment {

    ImageView markPresent, markAbsent;
    MarkAttendanceActions markListener;
    private int presentCount, absentCount;
    TextView presentCounter, absentCounter;
    View view;
    LayoutInflater inflater;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        presentCount = 0;
        absentCount = 0;

        markPresent = view.findViewById(R.id.attendance_check);
        markAbsent = view.findViewById(R.id.attendance_cross);

        markPresent.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            presentCount++;
            setCounters(presentCounter);

        });

        markAbsent.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            absentCount++;
            setCounters(absentCounter);
        });


        builder.setView(view)
                .setTitle("Mark Today's Attendance")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mark", (dialog, which) -> markListener.submitAttendance(presentCount, absentCount));


        return builder.create();
    }

    private void setCounters(TextView view){
        int initialCount = Integer.parseInt(view.getText().toString());
        view.setText(String.valueOf(initialCount+1));
    }

    @SuppressLint("InflateParams")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            markListener = (MarkAttendanceActions) context;
            inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
            view = inflater.inflate(R.layout.mark_attendance, null);
            presentCounter = view.findViewById(R.id.mark_present_counter);
            absentCounter = view.findViewById(R.id.mark_absent_counter);
            markListener.setCounters(presentCounter, absentCounter);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must Implement MarkAttendanceActions");
        }
    }

    public interface MarkAttendanceActions {
        void submitAttendance(int presentCount, int absentCount);
        void setCounters(TextView presentCounter, TextView absentCounter);
    }

}
