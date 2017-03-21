package com.wismna.geoffroy.donext.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.fragments.TodayFormDialogFragment;

import java.util.List;

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
        }

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    public void onNewTaskClick(View view) {
        List<Task> tasks;
        try(TaskDataAccess taskDataAccess = new TaskDataAccess(this)) {
            tasks = taskDataAccess.getAllTasks();
        }
        TodayFormDialogFragment taskDialogFragment =
                TodayFormDialogFragment.newInstance(tasks, TodayActivity.this);

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
    public void onTodayTaskDialogPositiveClick(DialogFragment dialog, View dialogView) {

    }
}
