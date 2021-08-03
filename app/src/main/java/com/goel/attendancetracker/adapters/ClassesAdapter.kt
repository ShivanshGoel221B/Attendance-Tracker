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
import com.goel.attendancetracker.models.ClassesModel
import java.util.*

class ClassesAdapter(
    private val classList: ArrayList<ClassesModel>,
    private val context: Context,
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {
    private var progress: Drawable? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.class_card, parent, false)
        val viewHolder = ClassViewHolder(view)
        viewHolder.downloadButton.setOnClickListener { clickListener.onDownloadClick(viewHolder.adapterPosition) }
        viewHolder.calendarButton.setOnClickListener { clickListener.onHistoryClick(viewHolder.adapterPosition) }
        viewHolder.editIcon.setOnClickListener { clickListener.onEditClick(viewHolder.adapterPosition) }
        viewHolder.markIcon.setOnClickListener { clickListener.onMarkClick(viewHolder.adapterPosition) }
        viewHolder.deleteIcon.setOnClickListener { clickListener.onDeleteClick(viewHolder.adapterPosition) }
        return viewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val model = classList[position]
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
        holder.className.text = model.name
        holder.classCounter.text = model.classCounter
    }

    override fun getItemCount(): Int = classList.size

    //VIEW HOLDER CLASS
    inner class ClassViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val attendanceProgressBar: ProgressBar = itemView.findViewById(R.id.class_progress)
        val requiredAttendanceBar: ProgressBar = itemView.findViewById(R.id.required_class_progress)
        val attendancePercentage: TextView = itemView.findViewById(R.id.class_attendance)
        val className: TextView = itemView.findViewById(R.id.class_name)
        val classCounter: TextView = itemView.findViewById(R.id.class_counter)
        val editIcon: ImageView = itemView.findViewById(R.id.class_edit_icon)
        val deleteIcon: ImageView = itemView.findViewById(R.id.class_delete_icon)
        val markIcon: ImageView = itemView.findViewById(R.id.class_mark_icon)
        val downloadButton: ImageView = itemView.findViewById(R.id.download_button)
        val calendarButton: ImageView = itemView.findViewById(R.id.calendar_button)
    }

    // ITEM CLICK LISTENER
    interface OnItemClickListener {
        fun onHistoryClick(position: Int)
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
        fun onMarkClick(position: Int)
        fun onDownloadClick(position: Int)
    }
}
