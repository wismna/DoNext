package com.wismna.geoffroy.donext.fragments;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.SmartFragmentStatePagerAdapter;
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
    private List<TaskList> taskLists;
    private boolean isHistory = false;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param isHistory Will this fragment show the task history?
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    // TODO: History should get its own activity and this fragment
    public static MainFragment newInstance(boolean isHistory) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putBoolean("history", isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void toggleHistory() {
        isHistory = !isHistory;
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHistory = getArguments().getBoolean("history");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = mView.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity);

        // Access database to retrieve Tabs
        try (TaskListDataAccess taskListDataAccess = new TaskListDataAccess(activity)) {
            taskLists = taskListDataAccess.getAllTaskLists();
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        if (taskLists.size() == 0) {
            TaskListsDialogFragment taskListFragment = new TaskListsDialogFragment();
            String title = getString(R.string.task_list_no_lists);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            // Set the arguments
            Bundle args = new Bundle();
            args.putInt("button_count", 1);
            args.putString("button_negative", getString(R.string.task_list_ok));
            taskListFragment.setArguments(args);

            taskListFragment.showFragment(fragmentManager, title, getResources().getBoolean(R.bool.large_layout));
        }
        else {
            int lastOpenedList = sharedPref.getInt("last_opened_tab", 0);
            // Set up the ViewPager with the sections adapter.
            mViewPager = mView.findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            // Open last opened tab
            mViewPager.setCurrentItem(lastOpenedList);

            if (!getResources().getBoolean(R.bool.large_layout)) {

                tabLayout = mView.findViewById(R.id.tabs);
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
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            if (scrollX == tabLayout.getChildAt(0).getMeasuredWidth() - tabLayout.getMeasuredWidth())
                rightArrow.setVisibility(View.INVISIBLE);
            else rightArrow.setVisibility(View.VISIBLE);
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
            return TasksFragment.newTaskListInstance(taskList.getId(), isHistory, MainFragment.this);
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
