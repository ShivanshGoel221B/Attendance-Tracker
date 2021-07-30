package com.goel.attendancetracker.dialogboxes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.goel.attendancetracker.R;

import java.util.Objects;

public class EditAttendanceDialog extends AppCompatDialogFragment {

    public static int presentCount, absentCount;
    TextView presentCounter, absentCounter;
    Button presentIncrease, presentDecrease, absentIncrease, absentDecrease;
    EditAttendanceDialog.SubmitNewAttendance submitListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        View view = inflater.inflate(R.layout.edit_attendance_dialog, null);
        setViews(view);

        setListeners();

        builder.setView(view)
                .setTitle("Edit Attendance")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Update", (dialog, which) -> submitListener.updateAttendance(presentCount, absentCount));

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            submitListener = (EditAttendanceDialog.SubmitNewAttendance) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must Implement MarkAttendanceActions");
        }
    }

    private void setViews(View view) {
        presentCounter = view.findViewById(R.id.dialog_present_counter);
        absentCounter = view.findViewById(R.id.dialog_absent_counter);
        presentIncrease = view.findViewById(R.id.present_increment);
        presentDecrease = view.findViewById(R.id.present_decrement);
        absentIncrease = view.findViewById(R.id.absent_increment);
        absentDecrease = view.findViewById(R.id.absent_decrement);

        updateCounters();
    }

    private void setListeners(){
        presentIncrease.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            presentCount++;
            updateCounters();
        });

        presentDecrease.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            if (presentCount == 0) return;
            presentCount--;
            updateCounters();
        });

        absentIncrease.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            absentCount++;
            updateCounters();
        });

        absentDecrease.setOnClickListener(v -> {
            v.performHapticFeedback(1);
            if (absentCount == 0) return;
            absentCount--;
            updateCounters();
        });
    }

    private void updateCounters() {
        presentCounter.setText(String.valueOf(presentCount));
        absentCounter.setText(String.valueOf(absentCount));
    }

    public interface SubmitNewAttendance{
        void updateAttendance(int newPresent, int newAbsent);
    }
}
