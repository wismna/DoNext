package com.wismna.geoffroy.donext.fragments

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.activities.HistoryActivity
import com.wismna.geoffroy.donext.dao.Task
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.widgets.InterceptTouchRelativeLayout
import org.joda.time.LocalDate

/**
 * Created by bg45 on 2017-03-21.
 * This is Task Form dynamic dialog fragment
 */
class TaskFormDialogFragment : DynamicDialogFragment() {
    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.  */
    interface NewTaskListener {
        fun onNewTaskDialogPositiveClick(dialog: DialogFragment, dialogView: View?)
        fun onNewTaskDialogNeutralClick(dialog: DialogFragment)
    }

    private var mListener: NewTaskListener? = null
    var task: Task? = null
        private set
    private var taskLists: List<TaskList>? = null
    private var listId = 0
    private var isToday = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContentLayoutId = R.layout.content_task_form
        val args = arguments
        if (args != null) {
            mButtonCount = args.getInt("button_count")
            mPositiveButtonString = getString(R.string.new_task_save)
            mNegativeButtonString = getString(R.string.new_task_cancel)
            if (task != null) mNeutralButtonString = getString(if (task!!.isHistory) R.string.new_task_restore else R.string.new_task_delete)
            listId = args.getInt("list")
            isToday = args.getBoolean("today")
        }
    }

    override fun onStart() {
        super.onStart()
        // Set Task Form specific information at that point because we are sure that the view is
        // entirely inflated (with the content fragment)
        val activity: Activity = requireActivity()
        if (activity is HistoryActivity) {
            val layout = findViewById<InterceptTouchRelativeLayout>(R.id.new_task_layout)
            layout.setInterceptTouchEvents(true)
        }
        setTaskValues(activity)
        clearFocus()
    }

    override fun onPositiveButtonClick(view: View?) {
        if (view == null) return
        val titleText = view.findViewById<EditText>(R.id.new_task_name)
        // handle confirmation button click here
        if (titleText.text.toString().matches("".toRegex())) titleText.error = resources.getString(R.string.new_task_name_error) else {
            // Send the positive button event back to the host activity
            mListener!!.onNewTaskDialogPositiveClick(this@TaskFormDialogFragment, view)
            dismiss()
        }
    }

    override fun onNeutralButtonClick(view: View?) {
        mListener!!.onNewTaskDialogNeutralClick(this@TaskFormDialogFragment)
    }

    override fun onNegativeButtonClick() {
        dismiss()
    }

    private fun setTaskValues(activity: Activity) {
        // Populate spinner with mTask lists
        val spinner = findViewById<Spinner>(R.id.new_task_list)
        // Hide spinner if only one mTask list
        if (taskLists!!.size <= 1) {
            spinner.visibility = View.GONE
            val taskListLabel = findViewById<TextView>(R.id.new_task_list_label)
            taskListLabel.visibility = View.GONE
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(
                activity, android.R.layout.simple_spinner_item, taskLists!!)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Auto set list value to current tab
        spinner.setSelection(listId)
        val checkBox = findViewById<CheckBox>(R.id.new_task_today)
        val todayLabel = findViewById<TextView>(R.id.new_task_today_label)
        val isTodayActive = isToday
        checkBox.visibility = if (isTodayActive) View.VISIBLE else View.GONE
        todayLabel.visibility = if (isTodayActive) View.VISIBLE else View.GONE

        // Get date picker
        val dueDatePicker = findViewById<DatePicker>(R.id.new_task_due_date)
        // Handle due date spinner depending on check box
        val setDueDate = findViewById<CheckBox>(R.id.new_task_due_date_set)
        setDueDate.setOnCheckedChangeListener { buttonView, isChecked -> dueDatePicker.visibility = if (isChecked) View.VISIBLE else View.GONE }

        // Handle priority changes
        val tooltip = findViewById<TextView>(R.id.new_task_priority_tooltip)
        val seekBar = findViewById<SeekBar>(R.id.new_task_priority)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tooltip.text = resources.getStringArray(R.array.task_priority)[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                tooltip.visibility = View.VISIBLE
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                tooltip.visibility = View.GONE
            }
        })
        tooltip.text = resources.getStringArray(R.array.task_priority)[seekBar.progress]
        // Set other properties if they exist
        if (task != null) {
            val titleText = findViewById<EditText>(R.id.new_task_name)
            titleText.setText(task!!.name)
            val descText = findViewById<EditText>(R.id.new_task_description)
            descText.setText(task!!.description)
            seekBar.progress = task!!.priority

            // Set Due Date
            val dueDate = task!!.dueDate
            if (dueDate != null) {
                setDueDate.isChecked = true
                dueDatePicker.updateDate(dueDate.year, dueDate.monthOfYear - 1, dueDate.dayOfMonth)
            }
            checkBox.isChecked = task!!.isToday
        } else {
            // Disallow past dates on new tasks
            dueDatePicker.minDate = LocalDate.now().toDate().time
        }
    }

    companion object {
        fun newInstance(task: Task?, taskLists: List<TaskList>?, newTaskListener: NewTaskListener?): TaskFormDialogFragment {
            val fragment = TaskFormDialogFragment()
            fragment.task = task
            fragment.taskLists = taskLists
            fragment.mListener = newTaskListener
            fragment.retainInstance = true
            return fragment
        }
    }
}
