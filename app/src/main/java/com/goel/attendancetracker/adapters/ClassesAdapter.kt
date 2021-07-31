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
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goel.attendancetracker.R
import com.goel.attendancetracker.models.ClassesModel
import java.util.*

class ClassesAdapter(private var classList: ArrayList<ClassesModel>, var context: Context) :
    RecyclerView.Adapter<ClassesAdapter.ClassViewHolder>() {
    private val clickListener = context as OnItemClickListener
    private var progress: Drawable? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.class_card, parent, false)
        return ClassViewHolder(view, clickListener)
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
        holder.editIcon.setImageResource(R.drawable.icon_edit)
        holder.deleteIcon.setImageResource(R.drawable.icon_delete)
        holder.markIcon.setImageResource(R.drawable.icon_mark)
    }

    override fun getItemCount(): Int {
        return classList.size
    }

    //VIEW HOLDER CLASS
    inner class ClassViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var attendanceProgressBar: ProgressBar = itemView.findViewById(R.id.class_progress)
        var requiredAttendanceBar: ProgressBar = itemView.findViewById(R.id.required_class_progress)
        var attendancePercentage: TextView = itemView.findViewById(R.id.class_attendance)
        var className: TextView = itemView.findViewById(R.id.class_name)
        var classCounter: TextView = itemView.findViewById(R.id.class_counter)
        var editIcon: ImageView = itemView.findViewById(R.id.class_edit_icon)
        var deleteIcon: ImageView = itemView.findViewById(R.id.class_delete_icon)
        var markIcon: ImageView = itemView.findViewById(R.id.class_mark_icon)
        private var menuIcon: ImageView = itemView.findViewById(R.id.context_menu_button)
        private fun setMenuItemClickListeners(
            popupMenu: PopupMenu,
            position: Int,
            listener: OnItemClickListener
        ) {
            popupMenu.setOnMenuItemClickListener { item ->
                if (position != RecyclerView.NO_POSITION) when (item.itemId) {
                    R.id.edit_history -> listener.onHistoryClick(position)
                    R.id.download_class_attendance -> listener.onDownloadClick(position)
                }
                true
            }
        }
        init {
            editIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onEditClick(position)
            }
            deleteIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onDeleteClick(position)
            }
            markIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) listener.onMarkClick(position)
            }
            menuIcon.setOnClickListener { v ->
                val popupMenu = PopupMenu(v.context, v)
                popupMenu.inflate(R.menu.class_context_menu)
                val position = adapterPosition
                setMenuItemClickListeners(popupMenu, position, listener)
                popupMenu.show()
            }
        }

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
