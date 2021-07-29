package com.goel.attendancetracker.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goel.attendancetracker.R
import com.goel.attendancetracker.models.OrganisationsModel
import com.goel.attendancetracker.adapters.OrganisationsAdapter.OrganisationViewHolder
import java.util.*

class OrganisationsAdapter(
    private var organisationList: ArrayList<OrganisationsModel>,
    var context: Context
) : RecyclerView.Adapter<OrganisationViewHolder>() {
    private var progress: Drawable? = null
    private var clickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganisationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.organisation_card, parent, false)
        return OrganisationViewHolder(view, clickListener)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrganisationViewHolder, position: Int) {
        val model = organisationList[position]
        val percentage = model.attendance
        val requiredPercentage = model.target
        progress = when {
            percentage >= requiredPercentage -> ContextCompat.getDrawable(
                context,
                R.drawable.attendance_progress
            )
            percentage > requiredPercentage * 0.75f -> ContextCompat.getDrawable(
                context, R.drawable.attendance_progress_low
            )
            else -> ContextCompat.getDrawable(
                context, R.drawable.attendance_progress_danger
            )
        }
        holder.attendancePercentage.text = "$percentage%"
        holder.attendanceProgressBar.progressDrawable = progress
        holder.attendanceProgressBar.progress = 0
        holder.attendanceProgressBar.progress = percentage
        holder.requiredAttendanceBar.progress = requiredPercentage
        holder.organisationName.text = model.name
        holder.editIcon.setImageResource(R.drawable.icon_edit)
        holder.deleteIcon.setImageResource(R.drawable.icon_delete)
    }

    override fun getItemCount(): Int {
        return organisationList.size
    }

    // ITEM CLICK LISTENER
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        clickListener = listener
    }

    //VIEW HOLDER CLASS
    class OrganisationViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var attendanceProgressBar: ProgressBar = itemView.findViewById(R.id.organisation_attendance_progress_bar)
        var requiredAttendanceBar: ProgressBar = itemView.findViewById(R.id.required_attendance_progress)
        var attendancePercentage: TextView = itemView.findViewById(R.id.organisation_attendance_percentage)
        var organisationName: TextView = itemView.findViewById(R.id.organisation_name)
        var editIcon: ImageView = itemView.findViewById(R.id.edit_organisation_icon)
        var deleteIcon: ImageView = itemView.findViewById(R.id.delete_organisation_icon)

        init {
            itemView.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) listener.onItemClick(position)
                }
            }
            deleteIcon.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) listener.onDeleteClick(position)
                }
            }
            editIcon.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) listener.onEditClick(position)
                }
            }
        }
    }
}