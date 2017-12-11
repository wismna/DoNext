package com.wismna.geoffroy.donext.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.fragments.MainFragment;
import com.wismna.geoffroy.donext.fragments.TaskFormDialogFragment;
import com.wismna.geoffroy.donext.fragments.TaskListsFragment;
import com.wismna.geoffroy.donext.fragments.TasksFragment;

/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Handles today list
        MenuItem todayListItem = menu.findItem(R.id.action_todayList);
        if (todayListItem == null) return false;
        todayListItem.setVisible(sharedPref.getBoolean("pref_conf_today_enable", false));

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

    /** Called when the user clicks on the Today List button */
    public void showTodayList(MenuItem item) {
        Intent intent = new Intent(this, TodayActivity.class);
        startActivity(intent);
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
        MainFragment fragment = getMainFragment();
        fragment.getViewPager().getAdapter().notifyDataSetChanged();
        //mSectionsPagerAdapter.notifyDataSetChanged();
    }

    /** Called when the user clicks the Edit Lists button  */
    public void openTaskLists(MenuItem menuItem) {
        /*Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);*/
        // Create the fragment
        TaskListsFragment taskListFragment = new TaskListsFragment();
        String title = getString(R.string.action_edit_task);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Set the arguments
        Bundle args = new Bundle();
        args.putBoolean("neutral", false);
        args.putString("button_positive", getString(R.string.new_task_save));
        args.putString("button_negative", getString(R.string.new_task_cancel));
        taskListFragment.setArguments(args);

        if (getResources().getBoolean(R.bool.large_layout))
            taskListFragment.show(fragmentManager, title);
        else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, taskListFragment, title)
                    .addToBackStack(null).commit();
        }
    }

    /** Called when the user clicks the History button*/
    public void openHistory(MenuItem item) {
        MainFragment fragment = getMainFragment();
        fragment.toggleHistory();
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

    /** Called when user clicks on the New Task floating button */
    public void onNewTaskClick(View view) {

        MainFragment fragment = getMainFragment();
        ViewPager viewPager = fragment.getViewPager();
        int currentTabPosition = viewPager.getCurrentItem();
        MainFragment.SectionsPagerAdapter pagerAdapter = (MainFragment.SectionsPagerAdapter) viewPager.getAdapter();
        TaskFormDialogFragment taskDialogFragment = TaskFormDialogFragment.newInstance(null,
                pagerAdapter.getAllItems(),
                (TasksFragment) pagerAdapter.getRegisteredFragment(currentTabPosition));

        // Set some configuration values for the tab
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Bundle args = new Bundle();
        args.putInt("list", currentTabPosition);
        args.putBoolean("today", sharedPref.getBoolean("pref_conf_today_enable", false));
        args.putBoolean("neutral", false);
        args.putString("button_positive", getString(R.string.new_task_save));
        args.putString("button_negative", getString(R.string.new_task_cancel));
        args.putString("button_neutral", getString(R.string.new_task_delete));
        taskDialogFragment.setArguments(args);

        String title = getString(R.string.action_new_task);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (getResources().getBoolean(R.bool.large_layout))
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

    private MainFragment getMainFragment(){
        FragmentManager manager = getSupportFragmentManager();
        return (MainFragment)manager.findFragmentById(R.id.fragment_main);
    }
}
