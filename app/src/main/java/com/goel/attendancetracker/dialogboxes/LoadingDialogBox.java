package com.goel.attendancetracker.dialogboxes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.goel.attendancetracker.R;

import java.util.Objects;

public class LoadingDialogBox extends AppCompatDialogFragment {

    public static String loadingText = "Loading...";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.loading_dialog_box, null);
        ((TextView) view.findViewById(R.id.loading_dialog_text)).setText(loadingText);
        builder.setView(view).setTitle("Please Wait");
        return builder.create();
    }
}
