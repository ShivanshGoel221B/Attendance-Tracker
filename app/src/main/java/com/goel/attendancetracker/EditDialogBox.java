package com.goel.attendancetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class EditDialogBox extends AppCompatDialogFragment {

    private EditText newNameText;
    private EditText newTargetText;
    private EditDialogListener listener;
    public static String name;
    public static int target;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        View view = inflater.inflate(R.layout.edit_dialog_box, null);
        newNameText = view.findViewById(R.id.edit_name);
        newTargetText = view.findViewById(R.id.edit_target);


        builder.setView(view)
                .setTitle("Edit")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.submitDetails(newNameText, newTargetText);
                    }
                });

        newNameText.setText(name);
        newTargetText.setText(String.valueOf(target));

        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must Implement EditDialogListener");
        }
    }

    public interface EditDialogListener{
        void submitDetails(EditText newNameText, EditText newTargetText);
    }
}
