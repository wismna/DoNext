package com.wismna.geoffroy.donext.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private TabLayout tabLayout;
    private List<TaskList> taskLists;
    private boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Handle Today list
        handleTodayList(sharedPref);

        // Access database to retrieve Tabs
        try (TaskListDataAccess taskListDataAccess = new TaskListDataAccess(this)) {
            taskLists = taskListDataAccess.getAllTaskLists();
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        if (taskLists.size() == 0) {
            Intent intent = new Intent(this, TaskListActivity.class);
            startActivity(intent);
        }
        else {

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            // Open last opened tab
            mViewPager.setCurrentItem(sharedPref.getInt("last_opened_tab", 0));

            View tabs = findViewById(R.id.tabs);
            if (tabs instanceof TabLayout) {
                tabLayout = (TabLayout) tabs;
                tabLayout.setupWithViewPager(mViewPager);

                // Handles scroll detection (only available for SDK version >=23)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toggleTabLayoutArrows(tabLayout.getScrollX());
                    //tabLayout.setScrollIndicators(TabLayout.SCROLL_INDICATOR_LEFT | TabLayout.SCROLL_INDICATOR_RIGHT);
                    tabLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            toggleTabLayoutArrows(scrollX);
                        }
                    });
                }
            }
            else if (tabs instanceof ListView) {
                ListView listView = (ListView) tabs;
                //listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskLists));
                listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_tasklist_item, taskLists));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mViewPager.setCurrentItem(position);
                        view.setSelected(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            parent.setElevation(10);
                        }
                    }
                });
            }
            // Hide or show new task floating button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // No tabs exist yet, nothing to save
        if (mViewPager == null) return;
        // Otherwise, save currently opened tab
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("last_opened_tab", mViewPager.getCurrentItem());
        editor.apply();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_changeLayout);
        if (item == null) return false;
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
        TaskDialogFragment taskDialogFragment = TaskDialogFragment.newInstance(null,
                mSectionsPagerAdapter.getAllItems(),
                (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(currentTabPosition));

        FragmentManager fragmentManager = getSupportFragmentManager();
        // Set current tab value to new task dialog
        Bundle args = new Bundle();
        args.putInt("list", currentTabPosition);
        taskDialogFragment.setArguments(args);

        if (mIsLargeLayout)
            taskDialogFragment.show(fragmentManager, getString(R.string.action_new_task));
        else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.replace(android.R.id.content, taskDialogFragment)
                    .addToBackStack(null).commit();
        }
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

    /** Toggles scrolling arrows visibility */
    private void toggleTabLayoutArrows(int scrollX){
        // Hide left arrow when scrolled to the left
        View leftArrow = findViewById(R.id.left_arrow);
        if (leftArrow != null) {
            if (scrollX <= 1) leftArrow.setVisibility(View.INVISIBLE);
            else leftArrow.setVisibility(View.VISIBLE);
        }
        // Hide right arrow when scrolled to the right
        View rightArrow = findViewById(R.id.right_arrow);
        if (rightArrow != null) {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            if (scrollX == tabLayout.getChildAt(0).getMeasuredWidth() - tabLayout.getMeasuredWidth())
                rightArrow.setVisibility(View.INVISIBLE);
            else rightArrow.setVisibility(View.VISIBLE);
        }
    }

    private void handleTodayList(SharedPreferences sharedPref) {
        String todayListName = getString(R.string.task_list_today);
        try (TaskListDataAccess taskListDataAccess = new TaskListDataAccess(this, TaskListDataAccess.MODE.WRITE)) {
            TaskList todayList = taskListDataAccess.getTaskListByName(todayListName);
            if (sharedPref.getBoolean("pref_conf_today_enable", false)) {
                // Get or create the Today list
                if (todayList == null) {
                    // TODO: set order correctly
                    todayList = taskListDataAccess.createTaskList(todayListName, 0);
                }
                if (!todayList.isVisible())
                    taskListDataAccess.updateVisibility(todayList.getId(), true);
            } else {
                // Hide the today list if it exists
                if (todayList != null) {
                    taskListDataAccess.updateVisibility(todayList.getId(), false);
                }
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends SmartFragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            TaskList taskList = taskLists.get(position);
            return TasksFragment.newInstance(taskList.getId(),
                    taskList.getName().equals(getString(R.string.task_list_today)), MainActivity.this);
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
