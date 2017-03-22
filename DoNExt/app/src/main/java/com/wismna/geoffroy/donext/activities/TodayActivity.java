package com.wismna.geoffroy.donext.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.fragments.TodayFormDialogFragment;

public class TodayActivity extends AppCompatActivity
    implements TodayFormDialogFragment.TodayTaskListener {

    private boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            // Enable the Up button
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_today, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem displayLayoutItem = menu.findItem(R.id.action_changeLayout);
        if (displayLayoutItem == null) return false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String layoutType = sharedPref.getString("pref_conf_task_layout", "1");
        switch (layoutType) {
            case "1" :
                displayLayoutItem.setIcon(R.drawable.ic_list_white_24dp);
                break;
            case "2" :
                displayLayoutItem.setIcon(R.drawable.ic_view_list_white_24dp);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
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
        //mSectionsPagerAdapter.notifyDataSetChanged();
        // Invalidate the menu to redraw the icon
        invalidateOptionsMenu();
    }


    public void onNewTaskClick(View view) {
        TodayFormDialogFragment taskDialogFragment =
                TodayFormDialogFragment.newInstance(this, TodayActivity.this);

        // Set some configuration values for the dialog
        Bundle args = new Bundle();
        args.putBoolean("layout", mIsLargeLayout);
        args.putString("button_positive", getString(R.string.new_task_save));
        args.putString("button_negative", getString(R.string.new_task_cancel));
        taskDialogFragment.setArguments(args);

        String title = getString(R.string.action_today_select);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mIsLargeLayout)
            taskDialogFragment.show(fragmentManager, title);
        else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, taskDialogFragment, title)
                    .addToBackStack(null).commit();
        }
    }

    @Override
    public void onTodayTaskDialogPositiveClick(View dialogView) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);
    }

    @Override
    public void onTodayTasksUpdated() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(true);
        try (TaskDataAccess taskDataAccess = new TaskDataAccess(this)) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.task_list_view);
            ((TaskRecyclerViewAdapter)recyclerView.getAdapter()).setItems(taskDataAccess.getTodayTasks());
        }
    }
}