package com.wismna.geoffroy.donext.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.SectionsPagerAdapter;
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;

import java.util.List;

/**
 * Fragment that will handle the main display
 */
public class MainFragment extends Fragment implements TasksFragment.TaskChangedAdapter {

    private View mView;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;

    public MainFragment() {
        // Required empty public constructor
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = mView.findViewById(R.id.toolbar);
        assert activity != null;
        activity.setSupportActionBar(toolbar);

        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity);

        // Access database to retrieve Tabs
        List<TaskList> taskLists;
        try (TaskListDataAccess taskListDataAccess = new TaskListDataAccess(activity)) {
            taskLists = taskListDataAccess.getAllTaskLists();

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), taskLists);
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        boolean isLargeLayout = getResources().getBoolean(R.bool.large_layout);

        // TODO: determine whether this is the first startup, and if so, show a tutorial of sorts
        // No tasks, show the edit task lists fragment
        if (taskLists.size() == 0) {
            TaskListsDialogFragment taskListFragment = new TaskListsDialogFragment();
            String title = getString(R.string.task_list_no_lists);
            FragmentManager fragmentManager = activity.getSupportFragmentManager();

            // Set the arguments
            Bundle args = new Bundle();
            args.putInt("button_count", 1);
            args.putString("button_negative", getString(R.string.task_list_ok));
            taskListFragment.setArguments(args);

            taskListFragment.showFragment(fragmentManager, title, getResources().getBoolean(R.bool.large_layout));
        }
        // Otherwise, show the normal view
        else {
            int lastOpenedList = sharedPref.getInt("last_opened_tab", 0);
            // Set up the ViewPager with the sections adapter.
            mViewPager = mView.findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            // Open last opened tab
            mViewPager.setCurrentItem(lastOpenedList);

            if (!isLargeLayout) {

                tabLayout = mView.findViewById(R.id.tabs);
                // Hide the tabs if there is only one task list
                if (taskLists.size() == 1) tabLayout.setVisibility(View.GONE);
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
            else {
                ListView listView = mView.findViewById(R.id.list);
                // Hide the list if there is only one task list
                if (taskLists.size() == 1) listView.setVisibility(View.GONE);
                //listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskLists));
                listView.setAdapter(new ArrayAdapter<>(activity, R.layout.list_tasklist_item, taskLists));
                //listView.setSelection(lastOpenedList);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mViewPager.setCurrentItem(position);
                    }
                });
            }
        }
        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();

        // No tabs exist yet, nothing to save
        if (mViewPager == null) return;
        // Otherwise, save currently opened tab
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("last_opened_tab", mViewPager.getCurrentItem());
        editor.apply();
    }

    @Override
    public void onTaskListChanged(Task task, int tabPosition) {
        TaskRecyclerViewAdapter destinationTaskAdapter = getSpecificTabAdapter(tabPosition);
        if (destinationTaskAdapter != null) destinationTaskAdapter.add(task, destinationTaskAdapter.getItemCount());
    }

    private TaskRecyclerViewAdapter getSpecificTabAdapter(int position) {
        TasksFragment taskFragment = (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(position);
        if (taskFragment == null) return null;
        View view = taskFragment.getView();
        if (view == null) return null;
        RecyclerView recyclerView = view.findViewById(R.id.task_list_view);
        if (recyclerView == null) return null;
        return (TaskRecyclerViewAdapter) recyclerView.getAdapter();
    }

    /** Toggles scrolling arrows visibility */
    private void toggleTabLayoutArrows(int scrollX){
        // Hide left arrow when scrolled to the left
        View leftArrow = mView.findViewById(R.id.left_arrow);
        if (leftArrow != null) {
            if (scrollX <= 1) leftArrow.setVisibility(View.INVISIBLE);
            else leftArrow.setVisibility(View.VISIBLE);
        }
        // Hide right arrow when scrolled to the right
        View rightArrow = mView.findViewById(R.id.right_arrow);
        if (rightArrow != null) {
            Point size = new Point();
            Activity activity = getActivity();
            assert activity != null;
            activity.getWindowManager().getDefaultDisplay().getSize(size);
            if (scrollX == tabLayout.getChildAt(0).getMeasuredWidth() - tabLayout.getMeasuredWidth())
                rightArrow.setVisibility(View.INVISIBLE);
            else rightArrow.setVisibility(View.VISIBLE);
        }
    }
}
