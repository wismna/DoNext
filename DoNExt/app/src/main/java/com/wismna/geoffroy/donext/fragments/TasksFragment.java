package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.activities.MainActivity;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;
import com.wismna.geoffroy.donext.helpers.TaskTouchHelper;
import com.wismna.geoffroy.donext.listeners.RecyclerItemClickListener;
import com.wismna.geoffroy.donext.widgets.DividerItemDecoration;
import com.wismna.geoffroy.donext.widgets.NoScrollingLayoutManager;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class TasksFragment extends Fragment implements
        TaskFormDialogFragment.NewTaskListener,
        ConfirmDialogFragment.ConfirmDialogListener,
        TaskTouchHelper.TaskTouchHelperAdapter{

    public interface TaskChangedAdapter {
        void onTaskListChanged(Task task, int tabPosition);
    }

    private static final String TASK_LIST_ID = "task_list_id";
    private long taskListId = -1;
    private boolean mIsLargeLayout;
    private boolean isTodayView = true;
    private TaskRecyclerViewAdapter taskRecyclerViewAdapter;
    private View view;
    private RecyclerView recyclerView;
    private TaskChangedAdapter mAdapter;
    private Snackbar snackbar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TasksFragment() {
    }

    public static TasksFragment newTaskListInstance(long taskListId, TaskChangedAdapter taskChangedAdapter) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putLong(TASK_LIST_ID, taskListId);
        fragment.setArguments(args);
        fragment.mAdapter = taskChangedAdapter;
        fragment.isTodayView = false;
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
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

        // Set RecyclerView Adapter
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get all tasks
        try (TaskDataAccess taskDataAccess = new TaskDataAccess(view.getContext())) {
            taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(
                    isTodayView? taskDataAccess.getTodayTasks() : taskDataAccess.getAllTasksFromList(taskListId),
                    Integer.valueOf(sharedPref.getString("pref_conf_task_layout", "1")));
        }
        recyclerView.setAdapter(taskRecyclerViewAdapter);

        // Set ItemTouch helper in RecyclerView to handle swipe move on elements
        ItemTouchHelper.Callback callback = new TaskTouchHelper(this);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        // Implements touch listener to add click detection
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                        Bundle args = new Bundle();
                        args.putInt("position", position);
                        args.putBoolean("layout", mIsLargeLayout);
                        args.putBoolean("today", sharedPref.getBoolean("pref_conf_today_enable", false));
                        args.putBoolean("neutral", true);
                        args.putString("button_positive", getString(R.string.new_task_save));
                        args.putString("button_negative", getString(R.string.new_task_cancel));
                        args.putString("button_neutral", getString(R.string.new_task_delete));

                        // Set current tab value to new task dialog
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        List<TaskList> taskLists;
                        Task task = taskRecyclerViewAdapter.getItem(position);
                        if (viewPager != null) {
                            taskLists = ((MainActivity.SectionsPagerAdapter) viewPager.getAdapter()).getAllItems();
                            args.putInt("list", viewPager.getCurrentItem());
                        }
                        else {
                            try (TaskListDataAccess taskListDataAccess = new TaskListDataAccess(getActivity())) {
                                taskLists = taskListDataAccess.getAllTaskLists();
                            }
                            for (TaskList taskList :
                                    taskLists) {
                                if (taskList.getId() == task.getTaskListId()) {
                                    args.putInt("list", taskLists.indexOf(taskList));
                                    break;
                                }
                            }
                        }

                        FragmentManager manager = getFragmentManager();
                        TaskFormDialogFragment taskDialogFragment = TaskFormDialogFragment.newInstance(
                                task, taskLists, TasksFragment.this);
                        taskDialogFragment.setArguments(args);

                        // Open the fragment as a dialog or as full-screen depending on screen size
                        String title = getString(R.string.action_edit_task);
                        if (mIsLargeLayout)
                            taskDialogFragment.show(manager, title);
                        else {
                            // The device is smaller, so show the fragment fullscreen
                            FragmentTransaction transaction = manager.beginTransaction();
                            // For a little polish, specify a transition animation
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            // To make it fullscreen, use the 'content' root view as the container
                            // for the fragment, which is always the root view for the activity
                            transaction.add(android.R.id.content, taskDialogFragment, title)
                                    .addToBackStack(null).commit();
                        }
                    }
                })
        );

        // Handle updating total counts in a listener to be sure that the layout is available
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // isAdded is tested to prevent an IllegalStateException when fast switching between tabs
                if (!isAdded()) return true;
                Resources resources = getResources();

                // Update total cycle count
                int totalCycles = taskRecyclerViewAdapter.getCycleCount();
                TextView totalCyclesView = (TextView) view.findViewById(R.id.total_task_cycles);
                if (totalCycles != 0)
                    totalCyclesView.setText(resources.getQuantityString(R.plurals.task_total_cycles, totalCycles, totalCycles));
                else totalCyclesView.setText("");

                // Update total tasks
                int totalTasks = taskRecyclerViewAdapter.getItemCount();
                TextView totalTasksView = (TextView) view.findViewById(R.id.total_task_count);
                if (totalTasks == 0) view.findViewById(R.id.no_more_tasks).setVisibility(View.VISIBLE);
                else totalTasksView.setText(resources.getQuantityString(R.plurals.task_total, totalTasks, totalTasks));

                // Update remaining tasks
                TextView remainingTasksView = (TextView) view.findViewById(R.id.remaining_task_count);
                NoScrollingLayoutManager layoutManager = (NoScrollingLayoutManager) recyclerView.getLayoutManager();
                int remainingTaskCount = totalTasks - layoutManager.findLastVisibleItemPosition() - 1;
                if (remainingTaskCount == 0) remainingTasksView.setText("");
                else remainingTasksView.setText(resources.getQuantityString(R.plurals.task_remaining, remainingTaskCount, remainingTaskCount));

                //recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        return view;
    }

    @Override
    public void onPause() {
        if (snackbar != null) snackbar.dismiss();
        super.onPause();
    }

    @Override
    public void onConfirmDialogClick(DialogFragment dialog, ConfirmDialogFragment.ButtonEvent event) {
        Bundle args = dialog.getArguments();
        int itemPosition = args.getInt("ItemPosition");
        int direction = args.getInt("Direction");

        // Handle never ask again checkbox
        CheckBox neverAskAgainCheckBox = (CheckBox) dialog.getDialog().findViewById(R.id.task_confirmation_never);
        if (neverAskAgainCheckBox.isChecked()) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            // Set system settings
            switch (direction) {
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
        }
        if (event == ConfirmDialogFragment.ButtonEvent.YES) {
            PerformTaskAction(itemPosition, direction);
        }
        else if(event == ConfirmDialogFragment.ButtonEvent.NO) {
            taskRecyclerViewAdapter.notifyItemChanged(itemPosition);
        }
    }

    @Override
    public void onNewTaskDialogPositiveClick(DialogFragment dialog, View dialogView) {
        // Get the dialog fragment
        if (dialogView == null) return;
        long id = 0;
        Task task = ((TaskFormDialogFragment)dialog).getTask();
        if (task != null) id = task.getId();

        // Get the controls
        Spinner listSpinner = (Spinner) dialogView.findViewById(R.id.new_task_list);
        EditText nameText = (EditText) dialogView.findViewById(R.id.new_task_name);
        EditText descText = (EditText) dialogView.findViewById(R.id.new_task_description);
        SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.new_task_priority);
        DatePicker dueDatePicker = (DatePicker) dialogView.findViewById(R.id.new_task_due_date);
        TaskList taskList = (TaskList) listSpinner.getSelectedItem();
        CheckBox todayList = (CheckBox) dialogView.findViewById(R.id.new_task_today);
        boolean isToday = todayList.isChecked();
        // Add the task to the database
        try (TaskDataAccess taskDataAccess = new TaskDataAccess(view.getContext(), TaskDataAccess.MODE.WRITE)) {
            Task newTask = taskDataAccess.createOrUpdateTask(id,
                    nameText.getText().toString(),
                    descText.getText().toString(),
                    seekBar.getProgress(),
                    taskList.getId(),
                    new LocalDate(dueDatePicker.getYear(), dueDatePicker.getMonth() + 1, dueDatePicker.getDayOfMonth()),
                    isToday);

            Bundle args = dialog.getArguments();
            // Should never happen because we will have to be on this tab to open the dialog
            if (taskRecyclerViewAdapter == null) return;

            // Add the task
            if (task == null) {
                // If the new task is added to another task list, update the tab
                if (mAdapter != null && taskListId != taskList.getId()) {
                    mAdapter.onTaskListChanged(newTask, listSpinner.getSelectedItemPosition());
                }
                // Otherwise add it to the current one
                else {
                    taskRecyclerViewAdapter.add(newTask, 0);
                    recyclerView.scrollToPosition(0);
                }
            }
            // Update the task
            else {
                int position = args.getInt("position");
                // Check if task list was changed
                if ((isTodayView && !isToday) || (!isTodayView && task.getTaskListId() != taskList.getId()))
                {
                    // Remove item from current tab
                    taskRecyclerViewAdapter.remove(position);

                    // Add it to the corresponding tab provided it is already instantiated
                    if (mAdapter != null) mAdapter.onTaskListChanged(newTask, listSpinner.getSelectedItemPosition());
                } else {
                    taskRecyclerViewAdapter.update(newTask, position);
                }
            }
        }
    }

    @Override
    public void onNewTaskDialogNeutralClick(DialogFragment dialog) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showDialog = sharedPref.getBoolean("pref_conf_del", true);
        Bundle args = dialog.getArguments();

        // Delete task from Adapter
        final int itemPosition = args.getInt("position");

        if (showDialog) {
            String title = getResources().getString(R.string.task_confirmation_delete_text);
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(this);
            Bundle confirmArgs = new Bundle();
            confirmArgs.putString("message", title);
            confirmArgs.putInt("button", R.string.task_confirmation_delete_button);
            confirmArgs.putInt("ItemPosition", itemPosition);
            confirmArgs.putInt("Direction", -1);
            confirmDialogFragment.setArguments(confirmArgs);
            confirmDialogFragment.show(getFragmentManager(), title);
        }
        else {
            PerformTaskAction(itemPosition, -1);
        }
    }

    @Override
    public void onItemSwiped(int itemPosition, int direction) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String title = "";
        boolean showDialog = false;
        int buttonLabel = -1;

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                title = getResources().getString(R.string.task_confirmation_done_text);
                showDialog = sharedPref.getBoolean("pref_conf_done", true);
                buttonLabel = R.string.task_confirmation_done_button;
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                title = getResources().getString(R.string.task_confirmation_next_text);
                showDialog = sharedPref.getBoolean("pref_conf_next", true);
                buttonLabel = R.string.task_confirmation_next_button;
                break;
        }
        if (showDialog) {
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(this);
            Bundle args = new Bundle();
            args.putString("message", title);
            args.putInt("button", buttonLabel);
            args.putInt("ItemPosition", itemPosition);
            args.putInt("Direction", direction);
            confirmDialogFragment.setArguments(args);
            confirmDialogFragment.show(getFragmentManager(), title);
        }
        else PerformTaskAction(itemPosition, direction);
    }

    /** Performs an action on a task: done, next or delete */
    private void PerformTaskAction(final int itemPosition, final int direction) {
        final long itemId = taskRecyclerViewAdapter.getItemId(itemPosition);
        final Task task = taskRecyclerViewAdapter.getItem(itemPosition);
        String action = "";
        Resources resources = getResources();

        taskRecyclerViewAdapter.remove(itemPosition);

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                action = resources.getString(R.string.snackabar_action_done);
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                action = resources.getString(R.string.snackabar_action_next);
                task.setCycle(task.getCycle() + 1);
                taskRecyclerViewAdapter.add(task, taskRecyclerViewAdapter.getItemCount());
                break;
            case -1:
                FragmentManager manager = getFragmentManager();
                DialogFragment dialog = (DialogFragment) manager.findFragmentByTag(getString(R.string.action_edit_task));
                if (dialog != null) dialog.dismiss();
                action = resources.getString(R.string.snackabar_action_deleted);
                break;
        }

        // Setup the snack bar
        snackbar = Snackbar.make(view, resources.getString(R.string.snackabar_label, action), Snackbar.LENGTH_LONG)
                .setAction(resources.getString(R.string.snackabar_button), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Undo adapter changes
                        switch (direction) {
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
                });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);

                // When clicked on undo, do not write to DB
                if (event == DISMISS_EVENT_ACTION) return;

                // Commit the changes to DB
                try (TaskDataAccess taskDataAccess = new TaskDataAccess(view.getContext(), TaskDataAccess.MODE.WRITE)) {
                    switch (direction) {
                        // Mark item as Done
                        case ItemTouchHelper.LEFT:
                            taskDataAccess.setDone(itemId);
                            break;
                        // Increase task cycle count
                        case ItemTouchHelper.RIGHT:
                            taskDataAccess.increaseCycle(task.getCycle(), itemId);
                            break;
                        case -1:
                            // Delete the task
                            taskDataAccess.deleteTask(itemId);
                    }
                }
            }
        }).show();
    }
}
