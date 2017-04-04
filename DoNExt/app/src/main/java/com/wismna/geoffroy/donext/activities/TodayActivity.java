package com.wismna.geoffroy.donext.activities;

import android.content.SharedPreferences;
import android.os.Build;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_today, menu);
        return true;
    }

    /** Called when the user clicks on the Change Layout button */
    public void changeLayout(MenuItem item) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String layoutTypeString = sharedPref.getString("pref_conf_task_layout", "1");
        int layoutType = Integer.valueOf(layoutTypeString);
        editor.putString("pref_conf_task_layout", String.valueOf(layoutType % 2 + 1));
        editor.apply();

        // TODO: find a less ugly way to refresh the list
        // Update the ViewPagerAdapter to refresh all tabs
        this.recreate();
    }


    public void onNewTaskClick(View view) {
        TodayFormDialogFragment taskDialogFragment =
                TodayFormDialogFragment.newInstance(TodayActivity.this);

        boolean isLargeLayout = getResources().getBoolean(R.bool.large_layout);
        // Set some configuration values for the dialog
        Bundle args = new Bundle();
        args.putBoolean("layout", isLargeLayout);
        args.putString("button_positive", getString(R.string.new_task_save));
        args.putString("button_negative", getString(R.string.new_task_cancel));
        taskDialogFragment.setArguments(args);

        String title = getString(R.string.action_today_select);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (isLargeLayout && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
