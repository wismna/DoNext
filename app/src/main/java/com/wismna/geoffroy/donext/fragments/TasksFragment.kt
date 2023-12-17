package com.wismna.geoffroy.donext.fragments

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.activities.HistoryActivity
import com.wismna.geoffroy.donext.activities.TodayActivity
import com.wismna.geoffroy.donext.adapters.SectionsPagerAdapter
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter
import com.wismna.geoffroy.donext.dao.Task
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.database.TaskDataAccess
import com.wismna.geoffroy.donext.database.TaskListDataAccess
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment.ButtonEvent
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment.ConfirmDialogListener
import com.wismna.geoffroy.donext.fragments.TaskFormDialogFragment.NewTaskListener
import com.wismna.geoffroy.donext.helpers.TaskTouchHelper
import com.wismna.geoffroy.donext.helpers.TaskTouchHelper.TaskTouchHelperAdapter
import com.wismna.geoffroy.donext.listeners.RecyclerItemClickListener
import org.joda.time.LocalDate
import java.util.Objects

/**
 * A fragment representing a list of Items.
 */
class TasksFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : Fragment(), NewTaskListener, ConfirmDialogListener, TaskTouchHelperAdapter {
    interface TaskChangedAdapter {
        fun onTaskListChanged(task: Task?, tabPosition: Int)
    }

    private var taskListId: Long = -1
    private var isTodayView = false
    private var isHistory = false
    private var taskRecyclerViewAdapter: TaskRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: TaskChangedAdapter? = null
    private var snackbar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            taskListId = requireArguments().getLong(TASK_LIST_ID)
        }
        val parentActivity: Activity? = activity
        if (parentActivity is HistoryActivity) isHistory = true
        if (parentActivity is TodayActivity) isTodayView = true
        mAdapter = parentFragment as MainFragment?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)
        val context = view.context

        // Set the Recycler view
        recyclerView = view.findViewById(R.id.task_list_view)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        TaskDataAccess(view.context).use { taskDataAccess ->
            taskRecyclerViewAdapter = TaskRecyclerViewAdapter(
                    if (isTodayView) taskDataAccess.todayTasks else taskDataAccess.getAllTasksFromList(taskListId, isHistory), isTodayView, isHistory)
        }
        recyclerView?.adapter = taskRecyclerViewAdapter
        if (!isHistory) {
            // Set ItemTouch helper in RecyclerView to handle swipe move on elements
            val callback: ItemTouchHelper.Callback = TaskTouchHelper(this,
                    ContextCompat.getColor(context, R.color.colorPrimary),
                    ContextCompat.getColor(context, R.color.colorAccent))
            val helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(recyclerView)
        }
        val resources = resources
        // Implements touch listener to add click detection
        recyclerView?.addOnItemTouchListener(
                RecyclerItemClickListener(context) { _: View?, position: Int ->
                    val isLargeLayout = resources.getBoolean(R.bool.large_layout)
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext())
                    val args = Bundle()
                    args.putBoolean("today", sharedPref.getBoolean("pref_conf_today_enable", false))
                    args.putInt("button_count", if (isHistory) 1 else 3)
                    args.putString("button_positive", getString(R.string.new_task_save))
                    args.putString("button_negative",
                            if (isHistory) getString(R.string.task_list_ok) else getString(R.string.new_task_cancel))
                    args.putString("button_neutral", getString(R.string.new_task_delete))
                    args.putInt("position", position)

                    // Set current tab value to new task dialog
                    val viewPager = requireActivity().findViewById<ViewPager2>(R.id.container)
                    var taskLists: List<TaskList>?
                    val task = taskRecyclerViewAdapter!!.getItem(position)
                    if (viewPager != null) {
                        taskLists = (Objects.requireNonNull(viewPager.adapter) as SectionsPagerAdapter).allItems
                        args.putInt("list", viewPager.currentItem)
                    } else {
                        TaskListDataAccess(activity).use { taskListDataAccess -> taskLists = taskListDataAccess.getTaskLists(isHistory) }
                        for (taskList in taskLists!!) {
                            if (taskList.id == task.taskListId) {
                                args.putInt("list", taskLists!!.indexOf(taskList))
                                break
                            }
                        }
                    }
                    val manager = parentFragmentManager
                    val taskDialogFragment: TaskFormDialogFragment = TaskFormDialogFragment.newInstance(
                            task, taskLists, this@TasksFragment)
                    taskDialogFragment.arguments = args

                    // Open the fragment as a dialog or as full-screen depending on screen size
                    taskDialogFragment.showFragment(manager,
                            getString(if (isHistory) R.string.action_view_task else R.string.action_edit_task), isLargeLayout)
                }
        )

        // Handle updating total counts in a listener to be sure that the layout is available
        recyclerView?.viewTreeObserver?.addOnPreDrawListener {

            // isAdded is tested to prevent an IllegalStateException when fast switching between tabs
            if (!isAdded) return@addOnPreDrawListener true
            val resources1 = getResources()

            // Update total cycle count
            val totalCycles = taskRecyclerViewAdapter!!.cycleCount
            val totalCyclesView = view.findViewById<TextView>(R.id.total_task_cycles)
            if (totalCycles != 0) totalCyclesView.text = resources1.getQuantityString(R.plurals.task_total_cycles, totalCycles, totalCycles) else totalCyclesView.text = ""

            // Update total tasks
            val totalTasks = taskRecyclerViewAdapter!!.itemCount
            val separator = view.findViewById<View>(R.id.main_additional_info_separator)
            val totalTasksView = view.findViewById<TextView>(R.id.total_task_count)
            val noMoreTasks = view.findViewById<View>(R.id.no_more_tasks)
            val createTasks = view.findViewById<View>(R.id.create_tasks)
            if (totalTasks == 0) {
                noMoreTasks.visibility = View.VISIBLE
                createTasks.visibility = View.VISIBLE
                totalTasksView.visibility = View.GONE
                separator.visibility = View.GONE
            } else {
                noMoreTasks.visibility = View.GONE
                createTasks.visibility = View.GONE
                totalTasksView.visibility = View.VISIBLE
                separator.visibility = View.VISIBLE
                totalTasksView.text = resources1.getQuantityString(R.plurals.task_total, totalTasks, totalTasks)
            }
            true
        }

        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        return view
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        if (snackbar != null) snackbar!!.dismiss()
        super.onPause()
    }

    override fun onConfirmDialogClick(dialog: DialogFragment, event: ButtonEvent) {
        val args = dialog.requireArguments()
        val itemPosition = args.getInt("ItemPosition")
        val direction = args.getInt("Direction")

        // Handle never ask again checkbox
        val neverAskAgainCheckBox = dialog.dialog?.findViewById<CheckBox>(R.id.task_confirmation_never)
        if (neverAskAgainCheckBox!!.isChecked) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPref.edit()
            when (direction) {
                ItemTouchHelper.LEFT -> editor.putBoolean("pref_conf_done", false)
                ItemTouchHelper.RIGHT -> editor.putBoolean("pref_conf_next", false)
                -1 -> editor.putBoolean("pref_conf_del", false)
            }
            editor.apply()
        }
        if (event == ButtonEvent.YES) {
            PerformTaskAction(itemPosition, direction)
        } else if (event == ButtonEvent.NO) {
            taskRecyclerViewAdapter!!.notifyItemChanged(itemPosition)
        }
    }

    override fun onNewTaskDialogPositiveClick(dialog: DialogFragment, dialogView: View?) {
        // Get the dialog fragment
        if (dialogView == null) return
        var id: Long = 0
        val task = (dialog as TaskFormDialogFragment).task
        if (task != null) id = task.id

        // Get the controls
        val listSpinner = dialogView.findViewById<Spinner>(R.id.new_task_list)
        val nameText = dialogView.findViewById<EditText>(R.id.new_task_name)
        val descText = dialogView.findViewById<EditText>(R.id.new_task_description)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.new_task_priority)
        val setDueDate = dialogView.findViewById<CheckBox>(R.id.new_task_due_date_set)
        val dueDatePicker = dialogView.findViewById<DatePicker>(R.id.new_task_due_date)
        val taskList = listSpinner.selectedItem as TaskList
        val todayList = dialogView.findViewById<CheckBox>(R.id.new_task_today)
        val isToday = todayList.isChecked
        TaskDataAccess(dialogView.context, TaskDataAccess.MODE.WRITE).use { taskDataAccess ->
            val newTask = taskDataAccess.createOrUpdateTask(id,
                    nameText.text.toString(),
                    descText.text.toString(),
                    seekBar.progress,
                    taskList.id,
                    if (setDueDate.isChecked) LocalDate(dueDatePicker.year,
                            dueDatePicker.month + 1,
                            dueDatePicker.dayOfMonth).toString() else "",
                    isToday)
            val args = dialog.getArguments()
            // Should never happen because we will have to be on this tab to open the dialog
            if (taskRecyclerViewAdapter == null) return
            var position = 0
            // Add the task
            if (task == null) {
                // If the new task is added to another task list, update the tab
                if (mAdapter != null && taskListId != taskList.id) {
                    mAdapter!!.onTaskListChanged(newTask, listSpinner.selectedItemPosition)
                } else {
                    position = taskRecyclerViewAdapter!!.itemCount
                    taskRecyclerViewAdapter!!.add(newTask, position)
                    recyclerView!!.scrollToPosition(position)
                }
            } else {
                position = args?.getInt("position") ?: 0
                // Check if task list was changed
                if (isTodayView && !isToday || !isTodayView && task.taskListId != taskList.id) {
                    // Remove item from current tab
                    taskRecyclerViewAdapter!!.remove(position)

                    // Add it to the corresponding tab provided it is already instantiated
                    if (mAdapter != null) mAdapter!!.onTaskListChanged(newTask, listSpinner.selectedItemPosition)
                } else {
                    taskRecyclerViewAdapter!!.update(newTask, position)
                }
            }
            taskRecyclerViewAdapter!!.notifyItemChanged(position)
        }
    }

    override fun onNewTaskDialogNeutralClick(dialog: DialogFragment) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val showDialog = sharedPref.getBoolean("pref_conf_del", true)
        val args = dialog.requireArguments()

        // Delete task from Adapter
        val itemPosition = args.getInt("position")
        if (showDialog) {
            val title = resources.getString(R.string.task_confirmation_delete_text)
            val confirmDialogFragment: ConfirmDialogFragment = ConfirmDialogFragment.newInstance(this)
            val confirmArgs = Bundle()
            confirmArgs.putString("message", title)
            confirmArgs.putInt("button", R.string.task_confirmation_delete_button)
            confirmArgs.putInt("ItemPosition", itemPosition)
            confirmArgs.putInt("Direction", -1)
            confirmDialogFragment.arguments = confirmArgs
            val fragmentManager = parentFragmentManager
            confirmDialogFragment.show(fragmentManager, title)
        } else {
            PerformTaskAction(itemPosition, -1)
        }
    }

    override fun onItemSwiped(position: Int, direction: Int) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var title = ""
        var showDialog = false
        var buttonLabel = -1
        when (direction) {
            ItemTouchHelper.LEFT -> {
                title = resources.getString(R.string.task_confirmation_done_text)
                showDialog = sharedPref.getBoolean("pref_conf_done", true)
                buttonLabel = R.string.task_confirmation_done_button
            }

            ItemTouchHelper.RIGHT -> {
                title = resources.getString(R.string.task_confirmation_next_text)
                showDialog = sharedPref.getBoolean("pref_conf_next", true)
                buttonLabel = R.string.task_confirmation_next_button
            }
        }
        if (showDialog) {
            val confirmDialogFragment: ConfirmDialogFragment = ConfirmDialogFragment.newInstance(this)
            val args = Bundle()
            args.putString("message", title)
            args.putInt("button", buttonLabel)
            args.putInt("ItemPosition", position)
            args.putInt("Direction", direction)
            confirmDialogFragment.arguments = args
            val fragmentManager = parentFragmentManager
            confirmDialogFragment.show(fragmentManager, title)
        } else PerformTaskAction(position, direction)
    }

    /** Performs an action on a task: done, next or delete  */
    private fun PerformTaskAction(itemPosition: Int, direction: Int) {
        val itemId = taskRecyclerViewAdapter!!.getItemId(itemPosition)
        val task = taskRecyclerViewAdapter!!.getItem(itemPosition)
        var action = ""
        val resources = resources
        taskRecyclerViewAdapter!!.remove(itemPosition)
        when (direction) {
            ItemTouchHelper.LEFT -> action = resources.getString(R.string.snackabar_action_done)
            ItemTouchHelper.RIGHT -> {
                action = resources.getString(R.string.snackabar_action_next)
                task.cycle = task.cycle + 1
                taskRecyclerViewAdapter!!.add(task, taskRecyclerViewAdapter!!.itemCount)
            }

            -1 -> {
                val manager = parentFragmentManager
                val dialog = Objects.requireNonNull(manager).findFragmentByTag(getString(R.string.action_edit_task)) as DialogFragment?
                dialog?.dismiss()
                action = resources.getString(R.string.snackabar_action_deleted)
            }
        }

        // Setup the snack bar
        val parentView = requireActivity().findViewById<View>(R.id.main_content)
        snackbar = Snackbar.make(parentView, resources.getString(R.string.snackabar_label, action), Snackbar.LENGTH_LONG)
                .setAction(resources.getString(R.string.snackabar_button)) { v: View? ->
                    when (direction) {
                        ItemTouchHelper.LEFT -> {}
                        ItemTouchHelper.RIGHT -> {
                            taskRecyclerViewAdapter!!.remove(taskRecyclerViewAdapter!!.itemCount - 1)
                            task.cycle = task.cycle - 1
                        }

                        -1 -> {}
                    }
                    // Reset the first item
                    taskRecyclerViewAdapter!!.add(task, itemPosition)
                    recyclerView!!.scrollToPosition(0)
                }
        snackbar!!.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar, event: Int) {
                super.onDismissed(snackbar, event)
                // When clicked on undo, do not write to DB
                if (event == DISMISS_EVENT_ACTION) return
                TaskDataAccess(parentView.context, TaskDataAccess.MODE.WRITE).use { taskDataAccess ->
                    when (direction) {
                        ItemTouchHelper.LEFT -> taskDataAccess.setDone(itemId, isTodayView)
                        ItemTouchHelper.RIGHT -> {
                            taskDataAccess.increaseCycle(task, isTodayView)
                            taskRecyclerViewAdapter!!.notifyItemChanged(taskRecyclerViewAdapter!!.itemCount - 1)
                        }

                        -1 ->  // Delete the task
                            taskDataAccess.deleteTask(itemId, isTodayView)
                    }
                }
            }
        }).show()
    }

    companion object {
        private const val TASK_LIST_ID = "task_list_id"
        fun newTaskListInstance(taskListId: Long): TasksFragment {
            val fragment = TasksFragment()
            val args = Bundle()
            args.putLong(TASK_LIST_ID, taskListId)
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}
