package com.wismna.geoffroy.donext.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.TaskListRecyclerViewAdapter.TaskViewHolder
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.helpers.TaskListTouchHelper.TaskListTouchHelperAdapter
import java.util.Collections

/**
 * [RecyclerView.Adapter] that can display a [TaskList].
 */
class TaskListRecyclerViewAdapter(private val mValues: MutableList<TaskList>,
                                  private val mListener: TaskListRecyclerViewAdapterListener) : RecyclerView.Adapter<TaskViewHolder>(), TaskListTouchHelperAdapter {
    interface TaskListRecyclerViewAdapterListener {
        fun onEditTextLoseFocus(taskList: TaskList)
        fun onClickDeleteButton(position: Int, id: Long)
        fun onItemMove(fromTaskId: Long, toTaskId: Long, fromPosition: Int, toPosition: Int)
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_tasklist, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mTaskCountView.text = mValues[position].taskCount.toString()
        holder.mTaskNameView.text = mValues[position].name

        // TODO: correct this...
        holder.handleView.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mListener.onStartDrag(holder)
            }
            false
        }

        // Handle inline name change
        holder.mTaskNameView.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            val name = editText.text.toString()
            val item = holder.mItem!!
            if (!hasFocus && item.name?.matches(name.toRegex())!!) {
                item.name = name
                update(item, holder.bindingAdapterPosition)
                mListener.onEditTextLoseFocus(item)
            }
        }

        // Handle click on delete button
        holder.mTaskDeleteButton.setOnClickListener { // Disable the OnFocusChanged listener as it is now pointless and harmful
            holder.mTaskNameView.onFocusChangeListener = null

            //remove(position);
            mListener.onClickDeleteButton(holder.bindingAdapterPosition, holder.mItem!!.id)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun add(item: TaskList, position: Int) {
        mValues.add(position, item)
        notifyItemInserted(position)
    }

    fun remove(position: Int) {
        mValues.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun update(item: TaskList, position: Int) {
        mValues[position] = item
        notifyItemChanged(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val fromTaskId = mValues[fromPosition].id
        val toTaskId = mValues[toPosition].id
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mValues, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mValues, i, i - 1)
            }
        }
        mListener.onItemMove(fromTaskId, toTaskId, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    class TaskViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val handleView: ImageView = itemView.findViewById(R.id.handle)
        val mTaskCountView: TextView
        val mTaskNameView: TextView
        val mTaskDeleteButton: Button
        var mItem: TaskList? = null

        init {
            mTaskCountView = mView.findViewById(R.id.task_list_count)
            mTaskNameView = mView.findViewById(R.id.task_list_name)
            mTaskDeleteButton = mView.findViewById(R.id.task_list_delete)
        }

        override fun toString(): String {
            return super.toString() + " '" + mTaskNameView.text + "'"
        }
    }
}
