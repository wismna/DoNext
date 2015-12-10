package com.wismna.geoffroy.donext.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.SmartFragmentStatePagerAdapter;
import com.wismna.geoffroy.donext.adapters.TaskAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment;
import com.wismna.geoffroy.donext.fragments.NewTaskFragment;
import com.wismna.geoffroy.donext.fragments.TasksFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NewTaskFragment.NewTaskListener,
        TasksFragment.OnListFragmentInteractionListener,
        ConfirmDialogFragment.ConfirmDialogListener
{

    protected TaskDataAccess taskDataAccess;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private List<TaskList> taskLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Access database to retrieve tasks
        taskDataAccess = new TaskDataAccess(this);
        taskDataAccess.open();

        // Access database to retrieve Tabs
        TaskListDataAccess taskListDataAccess = new TaskListDataAccess(this);
        taskListDataAccess.open();

        taskLists = taskListDataAccess.getAllTaskLists();
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Add Task floating button
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        taskDataAccess.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        taskDataAccess.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskDataAccess.close();
    }
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Get the dialog fragment
        Dialog dialogView = dialog.getDialog();
        // Get the controls
        Spinner listSpinner = (Spinner) dialogView.findViewById(R.id.new_task_list);
        EditText nameText = (EditText) dialogView.findViewById(R.id.new_task_name);
        EditText descText = (EditText) dialogView.findViewById(R.id.new_task_description);
        RadioGroup priorityGroup = (RadioGroup) dialogView.findViewById(R.id.new_task_priority);
        RadioButton priorityRadio = (RadioButton) dialogView.findViewById(priorityGroup.getCheckedRadioButtonId());
        TaskList taskList = (TaskList) listSpinner.getSelectedItem();
        // Add the task to the database
        Task task = taskDataAccess.createTask(
            nameText.getText().toString(),
            descText.getText().toString(),
            priorityRadio.getText().toString(),
            taskList.getId());

        // Update the corresponding tab adapter
        TasksFragment taskFragment = (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(listSpinner.getSelectedItemPosition());
        TaskAdapter taskAdapter = ((TaskAdapter)((RecyclerView)taskFragment.getView().findViewById(R.id.task_list_view)).getAdapter());
        taskAdapter.add(task, taskAdapter.getItemCount());
    }
    /** Called when user clicks on the New Task floating button */
    public void onNewTaskClick(View view) {
        OpenNewTaskDialog();
    }
    /** Called when the user clicks the Settings button  */
    public void openSettings(MenuItem menuItem) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    /** Called when the user clicks the Edit Lists button  */
    public void openTaskLists(MenuItem menuItem) {
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }

    /** Called when the user clicks the New Task button  */
    public void openNewTaskDialog(MenuItem menuItem) {
        OpenNewTaskDialog();
    }

    /** Will be called when the delete Task button is clicked */
    public void onDeleteTask(View view) {
        RecyclerView recyclerView = (RecyclerView) view;

    }

    @Override
    public void onListFragmentInteraction(Task item) {

    }

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {
        Bundle args = dialog.getArguments();
        int itemPosition = args.getInt("ItemPosition");
        int direction = args.getInt("Direction");

        TaskAdapter taskAdapter = ((ConfirmDialogFragment)dialog).getTaskAdapter();
        PerformSwipe(taskDataAccess, taskAdapter, itemPosition, direction);

    }

    @Override
    public void onDialogNeutralClick(android.support.v4.app.DialogFragment dialog) {
        Bundle args = dialog.getArguments();
        int itemPosition = args.getInt("ItemPosition");
        int direction = args.getInt("Direction");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("pref_conf_next", false);

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                editor.putBoolean("pref_conf_done", false);
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                editor.putBoolean("pref_conf_next", false);
                break;
        }
        editor.commit();
        TaskAdapter taskAdapter = ((ConfirmDialogFragment)dialog).getTaskAdapter();
        PerformSwipe(taskDataAccess, taskAdapter, itemPosition, direction);
    }

    private void OpenNewTaskDialog() {
        android.app.FragmentManager manager = getFragmentManager();
        NewTaskFragment newTaskFragment = new NewTaskFragment();

        // Set current tab value to new task dialog
        Bundle args = new Bundle();
        args.putInt("list", mViewPager.getCurrentItem());
        newTaskFragment.setArguments(args);

        newTaskFragment.show(manager, "Create new task");
    }

    public static void PerformSwipe(TaskDataAccess taskDataAccess, TaskAdapter taskAdapter, int itemPosition, int direction) {
        long itemId = taskAdapter.getItemId(itemPosition);
        taskDataAccess.open();
        Task task = taskAdapter.getItem(itemPosition);
        taskAdapter.remove(itemPosition);

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                taskDataAccess.setDone(itemId);
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                int cycle = task.getCycle();
                taskDataAccess.increaseCycle(cycle, itemId);
                task.setCycle(cycle + 1);
                int lastPosition = taskAdapter.getItemCount();
                taskAdapter.add(task, lastPosition);
                break;
        }
        taskDataAccess.close();
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends SmartFragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return TasksFragment.newInstance(taskLists.get(position).getId());
        }

        @Override
        public int getCount() {
            if (taskLists != null) {
                // Show the task lists
                return taskLists.size();
            }
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (taskLists == null) return "N/A";
            return taskLists.get(position).getName();
        }
    }
}
