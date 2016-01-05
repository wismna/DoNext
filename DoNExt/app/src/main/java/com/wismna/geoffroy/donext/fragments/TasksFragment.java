package com.wismna.geoffroy.donext.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.wismna.geoffroy.donext.ItemTouchHelpers.TaskTouchHelper;
import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.activities.MainActivity;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.listeners.RecyclerItemClickListener;
import com.wismna.geoffroy.donext.widgets.NoScrollingLayoutManager;

/**
 * A fragment representing a list of Items.
 */
public class TasksFragment extends Fragment implements
        TaskDialogFragment.NewTaskListener,
        ConfirmDialogFragment.ConfirmDialogListener,
        TaskTouchHelper.TaskTouchHelperAdapter {
    public interface TaskChangedAdapter {
        void onTaskListChanged(Task task, int tabPosition);
    }

    private static final String TASK_LIST_ID = "task_list_id";
    private long taskListId = -1;
    private TaskDataAccess taskDataAccess;
    private TaskRecyclerViewAdapter taskRecyclerViewAdapter;
    private View view;
    private RecyclerView recyclerView;
    private TaskChangedAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TasksFragment() {
    }

    public static TasksFragment newInstance(long taskListId, TaskChangedAdapter taskChangedAdapter) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putLong(TASK_LIST_ID, taskListId);
        fragment.setArguments(args);
        fragment.mAdapter = taskChangedAdapter;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            taskListId = getArguments().getLong(TASK_LIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tasks, container, false);
        final Context context = view.getContext();

        // Set the Recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.task_list_view);
        recyclerView.setLayoutManager(new NoScrollingLayoutManager(context));

        taskDataAccess = new TaskDataAccess(view.getContext());
        taskDataAccess.open();

        // Set total cycles
        UpdateCycleCount();

        // Set total count
        UpdateTaskCount();

        // Set RecyclerView Adapter
        taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(taskDataAccess.getAllTasks(taskListId));
        recyclerView.setAdapter(taskRecyclerViewAdapter);

        taskDataAccess.close();

        // Set ItemTouch helper in RecyclerView to handle swipe move on elements
        ItemTouchHelper.Callback callback = new TaskTouchHelper(this);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        // Implements touch listener to add click detection
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Bundle args = new Bundle();
                        args.putInt("position", position);

                        // Set current tab value to new task dialog
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        args.putInt("list", viewPager.getCurrentItem());

                        FragmentManager manager = getFragmentManager();
                        TaskDialogFragment taskDialogFragment = TaskDialogFragment.newInstance(
                                taskRecyclerViewAdapter.getItem(position),
                                ((MainActivity.SectionsPagerAdapter)viewPager.getAdapter()).getAllItems(), TasksFragment.this);

                        taskDialogFragment.setArguments(args);
                        taskDialogFragment.show(manager, "Edit task");
                    }
                })
        );
        return view;
    }

    private void UpdateCycleCount() {
        TextView totalCyclesView = (TextView) view.findViewById(R.id.total_task_cycles);
        totalCyclesView.setText(String.valueOf(taskDataAccess.getTotalCycles(taskListId) + " cycles"));
    }

    private void UpdateTaskCount() {
        TextView totalTasksView = (TextView) view.findViewById(R.id.total_task_count);
        totalTasksView.setText(String.valueOf(taskDataAccess.getTaskCount(taskListId) + " tasks"));
    }

    /** Performs an action on a task: done, next or delete */
    public void PerformTaskAction(final int itemPosition, final int direction) {
        final long itemId = taskRecyclerViewAdapter.getItemId(itemPosition);
        final Task task = taskRecyclerViewAdapter.getItem(itemPosition);
        String action = "";
        taskRecyclerViewAdapter.remove(itemPosition);

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                action = "done";
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                action = "nexted";
                task.setCycle(task.getCycle() + 1);
                taskRecyclerViewAdapter.add(task, taskRecyclerViewAdapter.getItemCount());
                break;
            case -1:
                action = "deleted";
                break;
        }

        // Setup the snack bar
        Snackbar.make(view, "Task " + action, Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Undo adapter changes
                        switch (direction)
                        {
                            // Nothing special to do for done
                            case ItemTouchHelper.LEFT:
                                break;
                            // Remove the last item
                            case ItemTouchHelper.RIGHT:
                                taskRecyclerViewAdapter.remove(taskRecyclerViewAdapter.getItemCount() - 1);
                                task.setCycle(task.getCycle() - 1);
                                break;
                            // Nothing special to do for delete
                            case -1:
                                break;
                        }
                        // Reset the first item
                        taskRecyclerViewAdapter.add(task, itemPosition);
                        recyclerView.scrollToPosition(0);
                    }
                }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);

                // When clicked on undo, do not write to DB
                if (event == DISMISS_EVENT_ACTION) return;

                taskDataAccess.open();
                // Commit the changes to DB
                switch (direction)
                {
                    // Mark item as Done
                    case ItemTouchHelper.LEFT:
                        taskDataAccess.setDone(itemId);
                        break;
                    // Increase task cycle count
                    case ItemTouchHelper.RIGHT:
                        taskDataAccess.increaseCycle(task.getCycle(), itemId);
                        break;
                    case -1:
                        // Commit the changes to DB
                        taskDataAccess.deleteTask(itemId);
                }

                UpdateCycleCount();
                UpdateTaskCount();

                taskDataAccess.close();
            }
        }).show();
    }

    @Override
    public void onConfirmDialogPositiveClick(DialogFragment dialog) {
        Bundle args = dialog.getArguments();
        int itemPosition = args.getInt("ItemPosition");
        int direction = args.getInt("Direction");

        PerformTaskAction(itemPosition, direction);
    }

    @Override
    public void onConfirmDialogNeutralClick(DialogFragment dialog) {
        Bundle args = dialog.getArguments();
        int itemPosition = args.getInt("ItemPosition");
        int direction = args.getInt("Direction");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        // Set system settings
        switch (direction)
        {
            case ItemTouchHelper.LEFT:
                editor.putBoolean("pref_conf_done", false);
                break;
            case ItemTouchHelper.RIGHT:
                editor.putBoolean("pref_conf_next", false);
                break;
            case -1:
                editor.putBoolean("pref_conf_del", false);
                break;
        }
        editor.apply();
        PerformTaskAction(itemPosition, direction);
    }

    @Override
    public void onConfirmDialogCancel(int position) {
        taskRecyclerViewAdapter.notifyItemChanged(position);
    }

    @Override
    public void onNewTaskDialogPositiveClick(DialogFragment dialog) {
        // Get the dialog fragment
        Dialog dialogView = dialog.getDialog();
        long id = 0;
        Task task = ((TaskDialogFragment)dialog).getTask();
        if (task != null) id = task.getId();

        // Get the controls
        Spinner listSpinner = (Spinner) dialogView.findViewById(R.id.new_task_list);
        EditText nameText = (EditText) dialogView.findViewById(R.id.new_task_name);
        EditText descText = (EditText) dialogView.findViewById(R.id.new_task_description);
        RadioGroup priorityGroup = (RadioGroup) dialogView.findViewById(R.id.new_task_priority);
        RadioButton priorityRadio = (RadioButton) dialogView.findViewById(priorityGroup.getCheckedRadioButtonId());
        TaskList taskList = (TaskList) listSpinner.getSelectedItem();

        // Add the task to the database
        taskDataAccess.open();
        Task newTask = taskDataAccess.createOrUpdateTask(id,
                nameText.getText().toString(),
                descText.getText().toString(),
                priorityRadio.getText().toString(),
                taskList.getId());

        UpdateTaskCount();
        taskDataAccess.close();
        // Update the corresponding tab adapter

        Bundle args = dialog.getArguments();
        // Should never happen because we will have to be on this tab to open the dialog
        if (taskRecyclerViewAdapter == null) return;

        // Add the task
        if (task == null)
            taskRecyclerViewAdapter.add(newTask, 0);
            // Update the task
        else {
            int position = args.getInt("position");
            // Check if task list was changed
            if (task.getTaskListId() != taskList.getId())
            {
                // Remove item from current tab
                taskRecyclerViewAdapter.remove(position);

                // Add it to the corresponding tab provided it is already instanciated
                mAdapter.onTaskListChanged(newTask, listSpinner.getSelectedItemPosition());
            }
            else taskRecyclerViewAdapter.update(newTask, position);
        }
    }

    @Override
    public void onNewTaskDialogNeutralClick(DialogFragment dialog) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String title = getResources().getString(R.string.task_confirmation_delete_text);
        boolean showDialog = sharedPref.getBoolean("pref_conf_del", true);
        Bundle args = dialog.getArguments();

        // Delete task from Adapter
        final int itemPosition = args.getInt("position");

        if (showDialog) {
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(title, this);
            Bundle confirmArgs = new Bundle();
            confirmArgs.putInt("ItemPosition", itemPosition);
            confirmArgs.putInt("Direction", -1);
            confirmDialogFragment.setArguments(confirmArgs);
            confirmDialogFragment.show(getFragmentManager(), title);
        }
        else PerformTaskAction(itemPosition, -1);
    }

    @Override
    public void onItemSwiped(int itemPosition, int direction) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String title = "";
        boolean showDialog = false;

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                title = "Mark task as done?";
                showDialog = sharedPref.getBoolean("pref_conf_done", true);
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                title = "Go to next task?";
                showDialog = sharedPref.getBoolean("pref_conf_next", true);
                break;
        }
        if (showDialog) {
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(title, this);
            Bundle args = new Bundle();
            args.putInt("ItemPosition", itemPosition);
            args.putInt("Direction", direction);
            confirmDialogFragment.setArguments(args);
            confirmDialogFragment.show(getFragmentManager(), title);
        }
        else PerformTaskAction(itemPosition, direction);
    }
}
