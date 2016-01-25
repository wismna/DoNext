package com.wismna.geoffroy.donext.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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
import android.view.View;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.SmartFragmentStatePagerAdapter;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;
import com.wismna.geoffroy.donext.fragments.TaskDialogFragment;
import com.wismna.geoffroy.donext.fragments.TasksFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TasksFragment.TaskChangedAdapter {

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

        // Access database to retrieve Tabs
        TaskListDataAccess taskListDataAccess = new TaskListDataAccess(this);
        taskListDataAccess.open();

        taskLists = taskListDataAccess.getAllTaskLists();
        mSectionsPagerAdapter.notifyDataSetChanged();
        taskListDataAccess.close();

        if (taskLists.size() == 0) {
            Intent intent = new Intent(this, TaskListActivity.class);
            startActivity(intent);
        }
        else {

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            // Open last opened tab
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            mViewPager.setCurrentItem(sharedPref.getInt("last_opened_tab", 0));

            // TODO: hide arrows on start when not needed
            final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
            tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    // Hide left arrow when scrolled to the left
                    View leftArrow = findViewById(R.id.left_arrow);
                    if (scrollX <= 1) leftArrow.setVisibility(View.GONE);
                    else leftArrow.setVisibility(View.VISIBLE);

                    // Hide right arrow when scrolled to the right
                    View rightArrow = findViewById(R.id.right_arrow);
                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);
                    if (scrollX == tabLayout.getChildAt(0).getMeasuredWidth() - size.x)
                        rightArrow.setVisibility(View.GONE);
                    else rightArrow.setVisibility(View.VISIBLE);
                }
            });

            // Hide or show new task floating button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save currently opened tab
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("last_opened_tab", mViewPager.getCurrentItem());
        editor.apply();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_changeLayout);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String layoutType = sharedPref.getString("pref_conf_task_layout", "1");
        switch (layoutType) {
            case "1" :
                item.setIcon(R.drawable.ic_list_white_24dp);
                break;
            case "2" :
                item.setIcon(R.drawable.ic_view_list_white_24dp);
                break;
        }

        return super.onPrepareOptionsMenu(menu);
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

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public void onTaskListChanged(Task task, int tabPosition) {
        TaskRecyclerViewAdapter destinationTaskAdapter = getSpecificTabAdapter(tabPosition);
        if (destinationTaskAdapter != null) destinationTaskAdapter.add(task, destinationTaskAdapter.getItemCount());
    }

    /** Called when user clicks on the New Task floating button */
    public void onNewTaskClick(View view) {
        int currentTabPosition = mViewPager.getCurrentItem();
        FragmentManager manager = getSupportFragmentManager();
        TaskDialogFragment taskDialogFragment = TaskDialogFragment.newInstance(null,
                mSectionsPagerAdapter.getAllItems(),
                (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(currentTabPosition));

        // Set current tab value to new task dialog
        Bundle args = new Bundle();
        args.putInt("list", currentTabPosition);
        taskDialogFragment.setArguments(args);

        taskDialogFragment.show(manager, "Create new task");
    }

    /** Called when the user clicks on the Change Layout button */
    public void changeLayout(MenuItem item) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String layoutTypeString = sharedPref.getString("pref_conf_task_layout", "1");
        int layoutType = Integer.valueOf(layoutTypeString);
        editor.putString("pref_conf_task_layout", String.valueOf(layoutType % 2 + 1));
        editor.apply();

        // Update the ViewPagerAdapter to refresh all tabs
        mSectionsPagerAdapter.notifyDataSetChanged();
        // Invalidate the menu to redraw the icon
        invalidateOptionsMenu();
    }

    /** Called when the user clicks the Edit Lists button  */
    public void openTaskLists(MenuItem menuItem) {
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }

    /** Called when the user clicks the Settings button  */
    public void openSettings(MenuItem menuItem) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /** Called when the user clicks the About button  */
    public void openAbout(MenuItem menuItem) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private TaskRecyclerViewAdapter getSpecificTabAdapter(int position) {
        TasksFragment taskFragment = (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(position);
        if (taskFragment == null) return null;
        View view = taskFragment.getView();
        if (view == null) return null;
        RecyclerView recyclerView = ((RecyclerView) view.findViewById(R.id.task_list_view));
        if (recyclerView == null) return null;
        return (TaskRecyclerViewAdapter) recyclerView.getAdapter();
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
            return TasksFragment.newInstance(taskLists.get(position).getId(), MainActivity.this);
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

        public List<TaskList> getAllItems(){
            return taskLists;
        }
    }
}
