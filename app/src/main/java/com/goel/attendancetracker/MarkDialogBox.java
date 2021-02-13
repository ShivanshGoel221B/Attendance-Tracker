package com.goel.attendancetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class MarkDialogBox extends AppCompatDialogFragment {

    ImageView markPresent, markAbsent;
    MarkDialogBox.SubmitAttendance markListener;
    private int presentCount, absentCount;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        View view = inflater.inflate(R.layout.mark_attendance, null);

        presentCount = 0;
        absentCount = 0;

        markPresent = view.findViewById(R.id.attendance_check);
        markAbsent = view.findViewById(R.id.attendance_cross);

        markPresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(1);
                presentCount++;
            }
        });

        markAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(1);
                absentCount++;
            }
        });


        builder.setView(view)
                .setTitle("Mark Attendance")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Mark", (dialog, which) -> markListener.submitAttendance(presentCount, absentCount));


        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            markListener = (MarkDialogBox.SubmitAttendance) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must Implement SubmitAttendance");
        }
    }

    public interface SubmitAttendance{
        void submitAttendance(int presentCount, int absentCount);
    }

}
