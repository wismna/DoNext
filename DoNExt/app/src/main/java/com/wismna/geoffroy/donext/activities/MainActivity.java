package com.wismna.geoffroy.donext.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.wismna.geoffroy.donext.fragments.NewTaskFragment;
import com.wismna.geoffroy.donext.fragments.TasksFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NewTaskFragment.NewTaskListener, TasksFragment.OnListFragmentInteractionListener {

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

    private TaskListDataAccess taskListDataAccess;
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
        taskListDataAccess = new TaskListDataAccess(this);
        taskListDataAccess.open();

        taskLists = taskListDataAccess.getAllTaskLists();
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
        TaskAdapter taskAdapter = ((TaskAdapter)((RecyclerView)taskFragment.getView()).getAdapter());
        taskAdapter.add(task, taskAdapter.getItemCount());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

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
        android.app.FragmentManager manager = getFragmentManager();
        NewTaskFragment newTaskFragment = new NewTaskFragment();

        // Set current tab value to new task dialog
        Bundle args = new Bundle();
        args.putInt("list", mViewPager.getCurrentItem());
        newTaskFragment.setArguments(args);

        newTaskFragment.show(manager, "Create new task");
    }

    @Override
    public void onListFragmentInteraction(Task item) {

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
