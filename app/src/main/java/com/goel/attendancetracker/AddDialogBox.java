package com.goel.attendancetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class AddDialogBox extends AppCompatDialogFragment {

    private EditText newNameText;
    private EditText newTargetText;
    private AddDialogBox.AddDialogListener listener;
    public static int overallAttendance = 100;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        View view = inflater.inflate(R.layout.edit_dialog_box, null);
        newNameText = view.findViewById(R.id.edit_name);
        newTargetText = view.findViewById(R.id.edit_target);


        builder.setView(view)
                .setTitle("Add New Class")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> listener.addSubmitDetails(newNameText, newTargetText));

        newTargetText.setText(String.valueOf(overallAttendance));

        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddDialogBox.AddDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must Implement AddDialogListener");
        }
    }

    public interface AddDialogListener{
        void addSubmitDetails(EditText newNameText, EditText newTargetText);
    }

}
