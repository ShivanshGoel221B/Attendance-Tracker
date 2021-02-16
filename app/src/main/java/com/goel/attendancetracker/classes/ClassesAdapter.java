package com.goel.attendancetracker.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goel.attendancetracker.R;

import java.util.ArrayList;

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.viewHolder> {

    ArrayList<ClassesModel> classList;
    Context context;
    private OnItemClickListener clickListener;
    Drawable progress;

    public ClassesAdapter(ArrayList<ClassesModel> classList, Context context) {
        this.classList = classList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.class_card, parent, false);
        return new viewHolder(view, clickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ClassesModel model = classList.get(position);
        int percentage = model.getClassAttendancePercentage();
        int requiredPercentage = model.getRequiredAttendance();
        if (percentage>=requiredPercentage)
            progress = ContextCompat.getDrawable(context, R.drawable.attendance_progress);
        else if (percentage > requiredPercentage*0.75f)
            progress = ContextCompat.getDrawable(context, R.drawable.attendance_progress_low);
        else
            progress = ContextCompat.getDrawable(context, R.drawable.attendance_progress_danger);
        holder.attendancePercentage.setText(percentage + "%");
        holder.attendanceProgressBar.setProgressDrawable(progress);
        holder.attendanceProgressBar.setProgress(0);
        holder.attendanceProgressBar.setProgress(percentage);
        holder.requiredAttendanceBar.setProgress(requiredPercentage);
        holder.className.setText(model.getClassName());
        holder.classCounter.setText(model.getClassCounter());
        holder.editIcon.setImageResource(R.drawable.icon_edit);
        holder.deleteIcon.setImageResource(R.drawable.icon_delete);
        holder.markIcon.setImageResource(R.drawable.icon_mark);
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    // ITEM CLICK LISTENER
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onEditClick(int position);
        void onMarkClick(int position);
    }
    public void setOnItemClickListener(ClassesAdapter.OnItemClickListener listener){ clickListener = listener; }

    //VIEW HOLDER CLASS
    public static class viewHolder extends RecyclerView.ViewHolder {

        ProgressBar attendanceProgressBar;
        ProgressBar requiredAttendanceBar;
        TextView attendancePercentage;
        TextView className;
        TextView classCounter;
        ImageView editIcon;
        ImageView deleteIcon;
        ImageView markIcon;


        public viewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            attendanceProgressBar = itemView.findViewById(R.id.class_progress);
            requiredAttendanceBar = itemView.findViewById(R.id.required_class_progress);
            attendancePercentage = itemView.findViewById(R.id.class_attendance);
            className = itemView.findViewById(R.id.class_name);
            classCounter = itemView.findViewById(R.id.class_counter);
            editIcon = itemView.findViewById(R.id.class_edit_icon);
            deleteIcon = itemView.findViewById(R.id.class_delete_icon);
            markIcon = itemView.findViewById(R.id.class_mark_icon);

            itemView.setOnClickListener(v -> {
                if (listener != null)
                {
                    int position = getAdapterPosition();
                    if (position !=RecyclerView.NO_POSITION)
                        listener.onItemClick(position);
                }
            });

            editIcon.setOnClickListener(v -> {
                if (listener != null)
                {
                    int position = getAdapterPosition();
                    if (position !=RecyclerView.NO_POSITION)
                        listener.onEditClick(position);
                }
            });

            deleteIcon.setOnClickListener(v -> {
                if (listener != null)
                {
                    int position = getAdapterPosition();
                    if (position !=RecyclerView.NO_POSITION)
                        listener.onDeleteClick(position);
                }
            });

            markIcon.setOnClickListener(v -> {
                if (listener != null)
                {
                    int position = getAdapterPosition();
                    if (position !=RecyclerView.NO_POSITION)
                        listener.onMarkClick(position);
                }
            });
        }
    }
}
