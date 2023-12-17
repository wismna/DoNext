package com.wismna.geoffroy.donext.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.TaskListRecyclerViewAdapter
import com.wismna.geoffroy.donext.adapters.TaskListRecyclerViewAdapter.TaskListRecyclerViewAdapterListener
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.database.TaskListDataAccess
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment.ButtonEvent
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment.ConfirmDialogListener
import com.wismna.geoffroy.donext.helpers.TaskListTouchHelper
import java.lang.ref.WeakReference

/**
 * A fragment representing a list of Items.
 */
class TaskListsDialogFragment
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
    : DynamicDialogFragment(), TaskListRecyclerViewAdapterListener, ConfirmDialogListener {
    private var taskListRecyclerViewAdapter: TaskListRecyclerViewAdapter? = null
    private var taskListDataAccess: TaskListDataAccess? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mListener: TaskListsListener? = null

    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.  */
    interface TaskListsListener {
        fun onTaskListsDialogNegativeClick()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mButtonCount = 1
        mNegativeButtonString = getString(R.string.task_list_ok)
        mContentLayoutId = R.layout.content_tasklists
        taskListDataAccess = TaskListDataAccess(context, TaskListDataAccess.MODE.WRITE)
        GetTaskListsTask(this).execute(taskListDataAccess)
    }

    override fun onStart() {
        super.onStart()
        val createTaskListButton = findViewById<Button>(R.id.new_task_list_button)
        createTaskListButton.setOnClickListener { v: View? ->
            val editText = findViewById<EditText>(R.id.new_task_list_name)
            val text = editText.text.toString()
            if (text.matches("".toRegex())) {
                editText.error = resources.getString(R.string.task_list_new_list_error)
                return@setOnClickListener
            }
            val position = taskListRecyclerViewAdapter!!.itemCount
            val taskList = taskListDataAccess!!.createTaskList(text, position)
            taskListRecyclerViewAdapter!!.add(taskList, position)
            editText.setText("")
            toggleVisibleCreateNewTaskListLayout()
        }
    }

    override fun onPositiveButtonClick(view: View?) {
        // Not implemented
    }

    override fun onNeutralButtonClick(view: View?) {
        // Not implemented
    }

    override fun onNegativeButtonClick() {
        dismiss()
        // TODO: add an argument to refresh only if something changed
        mListener!!.onTaskListsDialogNegativeClick()
    }

    override fun onPause() {
        clearFocus()
        super.onPause()
        taskListDataAccess!!.close()
    }

    override fun onResume() {
        super.onResume()
        clearFocus()
        taskListDataAccess!!.open(TaskListDataAccess.MODE.WRITE)
    }

    private fun toggleVisibleCreateNewTaskListLayout() {
        val layout = findViewById<LinearLayout>(R.id.new_task_list_layout)
        val taskListCount = taskListRecyclerViewAdapter!!.itemCount
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val maxTaskListsString = sharedPref.getString("pref_conf_max_lists", "5")
        val maxTaskLists = maxTaskListsString!!.toInt()
        if (taskListCount >= maxTaskLists) layout.visibility = View.GONE else layout.visibility = View.VISIBLE
        clearFocus()
    }

    override fun onEditTextLoseFocus(taskList: TaskList) {
        taskListDataAccess!!.updateName(taskList.id, taskList.name!!)
    }

    override fun onClickDeleteButton(position: Int, id: Long) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPref.getBoolean("pref_conf_tasklist_del", true)) {
            val title = resources.getString(R.string.task_list_confirmation_delete)
            val confirmDialogFragment: ConfirmDialogFragment = ConfirmDialogFragment.newInstance(this)
            val args = Bundle()
            args.putString("message", title)
            args.putInt("button", R.string.task_confirmation_delete_button)
            args.putInt("ItemPosition", position)
            args.putLong("ItemId", id)
            confirmDialogFragment.arguments = args
            confirmDialogFragment.show(parentFragmentManager, title)
        } else deleteTaskList(position, id)
    }

    override fun onConfirmDialogClick(dialog: DialogFragment, event: ButtonEvent) {
        // Handle never ask again checkbox
        val neverAskAgainCheckBox = dialog.dialog!!.findViewById<CheckBox>(R.id.task_confirmation_never)
        if (neverAskAgainCheckBox.isChecked) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPref.edit()
            editor.putBoolean("pref_conf_tasklist_del", false)
            editor.apply()
        }
        if (event == ButtonEvent.NO) return
        val args = dialog.requireArguments()
        deleteTaskList(args.getInt("ItemPosition"), args.getLong("ItemId"))
    }

    override fun onItemMove(fromTaskId: Long, toTaskId: Long, fromPosition: Int, toPosition: Int) {
        taskListDataAccess!!.updateOrder(fromTaskId, toPosition)
        taskListDataAccess!!.updateOrder(toTaskId, fromPosition)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    private fun deleteTaskList(position: Int, id: Long) {
        taskListRecyclerViewAdapter!!.remove(position)
        taskListDataAccess!!.deleteTaskList(id)
        toggleVisibleCreateNewTaskListLayout()
    }

    private class GetTaskListsTask constructor(context: TaskListsDialogFragment) : AsyncTask<TaskListDataAccess?, Void?, MutableList<TaskList>>() {
        private val fragmentReference: WeakReference<TaskListsDialogFragment>

        init {
            fragmentReference = WeakReference(context)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: TaskListDataAccess?): MutableList<TaskList>? {
            val taskListDataAccess = params[0]
            return taskListDataAccess?.getTaskLists(false)
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(taskLists: MutableList<TaskList>) {
            super.onPostExecute(taskLists)
            val fragment = fragmentReference.get() ?: return
            fragment.taskListRecyclerViewAdapter = TaskListRecyclerViewAdapter(taskLists, fragment)

            // Set the adapter
            val context = fragment.context
            val recyclerView = fragment.findViewById<RecyclerView>(R.id.task_lists_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = fragment.taskListRecyclerViewAdapter

            // Set the Touch Helper
            val callback: ItemTouchHelper.Callback = TaskListTouchHelper(fragment.taskListRecyclerViewAdapter!!)
            fragment.mItemTouchHelper = ItemTouchHelper(callback)
            fragment.mItemTouchHelper!!.attachToRecyclerView(recyclerView)
            fragment.toggleVisibleCreateNewTaskListLayout()
        }
    }

    companion object {
        fun newInstance(taskListListener: TaskListsListener?): TaskListsDialogFragment {
            val fragment = TaskListsDialogFragment()
            fragment.mListener = taskListListener
            return fragment
        }
    }
}
