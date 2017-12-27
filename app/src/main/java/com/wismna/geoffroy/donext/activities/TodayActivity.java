package com.wismna.geoffroy.donext.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.fragments.TodayFormDialogFragment;

import org.joda.time.LocalDate;

import java.util.Locale;

public class TodayActivity extends ToolBarActivityBase
    implements TodayFormDialogFragment.TodayTaskListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initToolBar();

        // Show the date
        TextView date = findViewById(R.id.today_date);
        date.setText(LocalDate.now().toString("EEEE, dd MMMM yyyy", getCurrentLocale()));

        // Set the no tasks texts
        TextView noTasks = findViewById(R.id.no_more_tasks);
        noTasks.setText(R.string.today_no_tasks);
        noTasks.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_smiley_satisfied_light, 0);
        noTasks.setCompoundDrawablePadding(10);

        TextView createTasks = findViewById(R.id.create_tasks);
        createTasks.setText(R.string.today_create_tasks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_today, menu);
        return true;
    }

    @Override
    public void onTodayTaskDialogPositiveClick(View dialogView) {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setEnabled(false);
    }

    @Override
    public void onTodayTasksUpdated() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setEnabled(true);
        try (TaskDataAccess taskDataAccess = new TaskDataAccess(this)) {
            RecyclerView recyclerView = findViewById(R.id.task_list_view);
            ((TaskRecyclerViewAdapter)recyclerView.getAdapter()).setItems(taskDataAccess.getTodayTasks());
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

        // TODO: find a less ugly way to refresh the list
        // Update the ViewPagerAdapter to refresh all tabs
        this.recreate();
    }

    public void onNewTaskClick(View view) {
        TodayFormDialogFragment taskDialogFragment =
                TodayFormDialogFragment.newInstance(TodayActivity.this);

        FragmentManager fragmentManager = getSupportFragmentManager();

        taskDialogFragment.showFragment(fragmentManager, getString(R.string.action_today_select), getResources().getBoolean(R.bool.large_layout));
    }


    private Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }
}
