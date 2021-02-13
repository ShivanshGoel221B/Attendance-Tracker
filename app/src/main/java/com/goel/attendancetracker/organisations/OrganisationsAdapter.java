                                                                                                                                            package com.goel.attendancetracker.organisations;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goel.attendancetracker.MainActivity;
import com.goel.attendancetracker.R;

import java.util.ArrayList;

public class OrganisationsAdapter extends RecyclerView.Adapter<OrganisationsAdapter.viewHolder> {

    ArrayList<OrganisationsModel> organisationList;
    Context context;
    Drawable progress;
    private OnItemClickListener clickListener;

    public OrganisationsAdapter(ArrayList<OrganisationsModel> organisationList, Context context) {
        this.organisationList = organisationList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.organisation_card, parent, false);
        return new viewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        OrganisationsModel model = organisationList.get(position);
        int percentage = model.getOrganisationAttendancePercentage();
        int requiredPercentage = model.getRequiredAttendance();
        if (percentage>=requiredPercentage)
            progress = context.getDrawable(R.drawable.attendance_progress);
        else if (percentage > requiredPercentage*0.75f)
            progress = context.getDrawable(R.drawable.attendance_progress_low);
        else
            progress = context.getDrawable(R.drawable.attendance_progress_danger);
        holder.attendancePercentage.setText(percentage + "%");
        holder.attendanceProgressBar.setProgressDrawable(progress);
        holder.attendanceProgressBar.setProgress(percentage);
        holder.requiredAttendanceBar.setProgress(requiredPercentage);
        holder.organisationName.setText(model.getOrganisationName());
        holder.editIcon.setImageResource(R.drawable.icon_edit);
        holder.deleteIcon.setImageResource(R.drawable.icon_delete);
    }

    @Override
    public int getItemCount() {
        return organisationList.size();
    }

    // ITEM CLICK LISTENER
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onEditClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){ clickListener = listener; }

    //VIEW HOLDER CLASS
    public static class viewHolder extends RecyclerView.ViewHolder{

        ProgressBar attendanceProgressBar;
        ProgressBar requiredAttendanceBar;
        TextView attendancePercentage;
        TextView organisationName;
        ImageView editIcon;
        ImageView deleteIcon;

        public viewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            attendanceProgressBar = itemView.findViewById(R.id.organisation_attendance_progress_bar);
            requiredAttendanceBar = itemView.findViewById(R.id.required_attendance_progress);
            attendancePercentage = itemView.findViewById(R.id.organisation_attendance_percentage);
            organisationName = itemView.findViewById(R.id.organisation_name);
            editIcon = itemView.findViewById(R.id.edit_organisation_icon);
            deleteIcon = itemView.findViewById(R.id.delete_organisation_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                    {
                        int position = getAdapterPosition();
                        if (position !=RecyclerView.NO_POSITION)
                            listener.onItemClick(position);
                    }
                }
            });

            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                    {
                        int position = getAdapterPosition();
                        if (position !=RecyclerView.NO_POSITION)
                            listener.onDeleteClick(position);
                    }
                }
            });

            editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                    {
                        int position = getAdapterPosition();
                        if (position !=RecyclerView.NO_POSITION)
                            listener.onEditClick(position);
                    }
                }
            });
        }
    }
}
