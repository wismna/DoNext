package com.wismna.geoffroy.donext.fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.TodayArrayAdapter
import com.wismna.geoffroy.donext.dao.Task
import com.wismna.geoffroy.donext.database.TaskDataAccess
import org.joda.time.LocalDate
import java.lang.ref.WeakReference

/**
 * Created by bg45 on 2017-03-21.
 * This is the Today Form dynamic dialog fragment
 */
class TodayFormDialogFragment : DynamicDialogFragment() {
    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.  */
    interface TodayTaskListener {
        fun onTodayTaskDialogPositiveClick(dialogView: View?)
        fun onTodayTasksUpdated()
    }

    private var mListener: TodayTaskListener? = null
    private val mUpdatedTasks: MutableList<Task> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPositiveButtonString = getString(R.string.new_task_save)
        mNegativeButtonString = getString(R.string.new_task_cancel)
        mContentLayoutId = R.layout.content_today_form
        // Load the tasks asynchronously
        LoadTasks(this).execute(activity)
    }

    private fun setLayoutValues(tasks: List<Task?>) {
        val editText = findViewById<EditText>(R.id.today_search)
        val listView = findViewById<ListView>(R.id.today_tasks)
        val adapter = TodayArrayAdapter(requireActivity(), tasks)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            // Set Today date for the task
            val task = adapter.getItem(position) ?: return@OnItemClickListener
            task.setTodayDate(if (task.isToday) "" else LocalDate.now().toString())
            // Maintain a list of actually updated tasks to commit to DB
            if (!mUpdatedTasks.contains(task)) mUpdatedTasks.add(task) else mUpdatedTasks.remove(task)
            // Refresh the view
            adapter.notifyDataSetChanged()
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onPositiveButtonClick(view: View?) {
        mListener!!.onTodayTaskDialogPositiveClick(view)
        // Only commit the updated tasks to DB
        UpdateTasks(this).execute(*mUpdatedTasks.toTypedArray<Task>())
        dismiss()
    }

    override fun onNeutralButtonClick(view: View?) {}
    override fun onNegativeButtonClick() {
        dismiss()
    }

    internal class LoadTasks(context: TodayFormDialogFragment) : AsyncTask<Context?, Void?, List<Task?>>() {
        private val fragmentReference: WeakReference<TodayFormDialogFragment>

        init {
            fragmentReference = WeakReference(context)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Context?): List<Task?> {
            TaskDataAccess(params[0]).use { taskDataAccess -> return taskDataAccess.allTasks }
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(tasks: List<Task?>) {
            super.onPostExecute(tasks)
            fragmentReference.get()!!.setLayoutValues(tasks)
        }

    }

    private class UpdateTasks(context: TodayFormDialogFragment) : AsyncTask<Task?, Void?, Int>() {
        private val fragmentReference: WeakReference<TodayFormDialogFragment>

        init {
            fragmentReference = WeakReference(context)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Task?): Int {
            var position: Int
            TaskDataAccess(fragmentReference.get()!!.activity, TaskDataAccess.MODE.WRITE).use { taskDataAccess ->
                position = 0
                while (position < params.size) {
                    val task = params[position]
                    if (task != null) {
                        taskDataAccess.updateTodayTasks(task.id, task.isToday, position)
                    }
                    position++
                }
            }
            return position
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(integer: Int) {
            super.onPostExecute(integer)
            fragmentReference.get()!!.mListener!!.onTodayTasksUpdated()
        }

    }

    companion object {
        fun newInstance(todayTaskListener: TodayTaskListener?): TodayFormDialogFragment {
            val fragment = TodayFormDialogFragment()
            fragment.mListener = todayTaskListener
            fragment.retainInstance = true
            return fragment
        }
    }
}
