package com.wismna.geoffroy.donext.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter.StandardViewHolder
import com.wismna.geoffroy.donext.dao.Task
import org.joda.time.LocalDate

/**
 * [RecyclerView.Adapter] that can display a [Task].
 */
class TaskRecyclerViewAdapter(private var mValues: MutableList<Task>, private val mIsToday: Boolean, private val mIsHistory: Boolean) : RecyclerView.Adapter<StandardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandardViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
        return StandardViewHolder(view)
    }

    override fun onBindViewHolder(holder: StandardViewHolder, position: Int) {
        // Set basic information
        holder.mItem = mValues[position]
        holder.mIdView.text = holder.mItem!!.id.toString()
        holder.mCycleView.text = holder.mItem!!.cycle.toString()
        holder.mTitleView.text = holder.mItem!!.name
        holder.mDescriptionView.text = holder.mItem!!.description
        // Set task rendering
        if (position > 0) {
            holder.mTitleView.typeface = Typeface.DEFAULT
            holder.mTitleView.setTextColor(Color.BLACK)
        }
        when (holder.mItem!!.priority) {
            0 -> holder.mIconView.setImageResource(R.drawable.ic_low_priority_lightgray_24dp)
            2 -> holder.mIconView.setImageResource(R.drawable.ic_priority_high_red_24dp)
            else -> holder.mIconView.setImageDrawable(null)
        }

        // Additional information will not be displayed in Today view
        if (mIsToday) return
        // Set alarm if past due date
        val dueDate = holder.mItem!!.dueDate
        if (holder.mItem!!.done == 1) holder.mIconView.setImageResource(R.drawable.ic_check_ligth) else if (holder.mItem!!.deleted == 1) holder.mIconView.setImageResource(R.drawable.ic_close_light) else if (dueDate != null && dueDate.isBefore(LocalDate.now())) holder.mIconView.setImageResource(R.drawable.ic_access_alarm)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && !mIsHistory) R.layout.fragment_task_first else R.layout.fragment_task_detailed
    }

    fun add(item: Task, position: Int) {
        mValues.add(position, item)
        notifyItemInserted(position)
    }

    fun remove(position: Int) {
        mValues.removeAt(position)
        notifyItemRemoved(position)
    }

    fun update(item: Task, position: Int) {
        mValues[position] = item
        notifyItemChanged(position)
    }

    val cycleCount: Int
        get() {
            var count = 0
            for (task in mValues) {
                count += task.cycle
            }
            return count
        }

    fun setItems(tasks: MutableList<Task>) {
        mValues = tasks
        notifyItemRangeInserted(0, tasks.size - 1)
    }

    fun getItem(position: Int): Task {
        return mValues[position]
    }

    class StandardViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView
        val mIconView: ImageView
        val mCycleView: TextView
        val mTitleView: TextView
        val mDescriptionView: TextView
        var mItem: Task? = null

        init {
            mIdView = mView.findViewById(R.id.task_id)
            mIconView = mView.findViewById(R.id.task_icon)
            mCycleView = mView.findViewById(R.id.task_cycle)
            mTitleView = mView.findViewById(R.id.task_name)
            mDescriptionView = mView.findViewById(R.id.task_description)
        }

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }
}
