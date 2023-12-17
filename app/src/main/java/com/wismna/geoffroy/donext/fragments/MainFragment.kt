package com.wismna.geoffroy.donext.fragments

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.activities.HistoryActivity
import com.wismna.geoffroy.donext.adapters.SectionsPagerAdapter
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter
import com.wismna.geoffroy.donext.dao.Task
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.database.TaskListDataAccess
import com.wismna.geoffroy.donext.fragments.TaskListsDialogFragment.TaskListsListener
import com.wismna.geoffroy.donext.fragments.TasksFragment.TaskChangedAdapter

/**
 * Fragment that will handle the main display
 */
class MainFragment : Fragment(), TaskChangedAdapter, TaskListsListener {
    private var mView: View? = null
    var viewPager: ViewPager2? = null
        private set
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_main, container, false)
        val activity = requireActivity() as AppCompatActivity
        if (activity is HistoryActivity) {
            val actionBar = activity.getSupportActionBar()!!
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }

        // Get preferences
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)

        // Check if this is the first time loading this app
        val firstTime = sharedPref.getBoolean("first_time", true)
        // If it is, create a default task list
        if (firstTime) {
            TaskListDataAccess(activity).use { taskListDataAccess ->
                taskListDataAccess.createTaskList(getString(R.string.default_task_list_name), 0)
                val editor = sharedPref.edit()
                editor.putBoolean("first_time", false)
                editor.apply()
            }
        }

        // Load task lists
        updateTaskLists(activity)
        if (!firstTime) {
            // Open last opened tab
            val lastOpenedList = sharedPref.getInt("last_opened_tab", 0)
            viewPager!!.currentItem = lastOpenedList
        }
        return mView
    }

    override fun onPause() {
        super.onPause()

        // No tabs exist yet, nothing to save
        if (viewPager == null) return
        // Otherwise, save currently opened tab
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putInt("last_opened_tab", viewPager!!.currentItem)
        editor.apply()
    }

    override fun onTaskListChanged(task: Task?, tabPosition: Int) {
        val destinationTaskAdapter = getSpecificTabAdapter(tabPosition)
        destinationTaskAdapter?.add(task!!, destinationTaskAdapter.itemCount)
    }

    override fun onTaskListsDialogNegativeClick() {
        val activity = requireActivity() as AppCompatActivity
        updateTaskLists(activity)
    }

    private fun updateTaskLists(activity: AppCompatActivity) {
        val isHistoryActivity = activity is HistoryActivity
        // Access database to retrieve Tabs
        var taskLists: List<TaskList>
        TaskListDataAccess(activity).use { taskListDataAccess ->
            taskLists = taskListDataAccess.getTaskLists(isHistoryActivity)

            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = SectionsPagerAdapter(this, taskLists)
        }

        // Set up the ViewPager with the sections adapter.
        viewPager = mView?.findViewById(R.id.container)
        viewPager?.adapter = mSectionsPagerAdapter
        if (!resources.getBoolean(R.bool.large_layout)) {
            val tabLayout = mView!!.findViewById<TabLayout>(R.id.tabs)
            // Hide the tabs if there is only one task list
            tabLayout.visibility = if (taskLists.size == 1 && !isHistoryActivity) View.GONE else View.VISIBLE
            //tabLayout.setupWithViewPager(mViewPager);
            viewPager?.let {
                TabLayoutMediator(tabLayout, it
                ) { tab: TabLayout.Tab, position: Int -> tab.setText(mSectionsPagerAdapter!!.allItems!![position].name) }.attach()
            }

            // Handles scroll detection (only available for SDK version >=23)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tabLayout.scrollIndicators = TabLayout.SCROLL_INDICATOR_LEFT or TabLayout.SCROLL_INDICATOR_RIGHT
            }
        } else {
            // Move guideline to hide task list
            val guideline = mView!!.findViewById<Guideline>(R.id.center_guideline)
            val lp = guideline.layoutParams as ConstraintLayout.LayoutParams
            lp.guidePercent = if (taskLists.size == 1 && !isHistoryActivity) 0F else 0.2.toFloat()
            guideline.layoutParams = lp
            val listView = mView!!.findViewById<ListView>(R.id.list)
            // Hide the list if there is only one task list
            listView.adapter = ArrayAdapter(activity, R.layout.list_tasklist_item, taskLists)
            //listView.setSelection(lastOpenedList);
            listView.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long -> viewPager?.currentItem = position }
        }
    }

    private fun getSpecificTabAdapter(position: Int): TaskRecyclerViewAdapter? {
        //TasksFragment taskFragment = (TasksFragment) mSectionsPagerAdapter.getRegisteredFragment(position);
        val taskFragment = mSectionsPagerAdapter!!.createFragment(position) as TasksFragment
        //if (taskFragment == null) return null;
        val view = taskFragment.view ?: return null
        val recyclerView = view.findViewById<RecyclerView>(R.id.task_list_view) ?: return null
        return recyclerView.adapter as TaskRecyclerViewAdapter?
    }
}
