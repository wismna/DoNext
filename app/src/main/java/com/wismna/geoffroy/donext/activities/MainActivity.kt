package com.wismna.geoffroy.donext.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.SectionsPagerAdapter
import com.wismna.geoffroy.donext.fragments.AboutDialogFragment
import com.wismna.geoffroy.donext.fragments.MainFragment
import com.wismna.geoffroy.donext.fragments.TaskFormDialogFragment
import com.wismna.geoffroy.donext.fragments.TaskListsDialogFragment
import com.wismna.geoffroy.donext.fragments.TasksFragment

/**
 * Main Activity class
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App center integration
        AppCenter.start(application, "a5aeb8b9-0730-419f-b30b-f23b972f82f3",
                Analytics::class.java, Crashes::class.java)
        setContentView(R.layout.activity_main)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Handles today list
        val todayListItem = menu.findItem(R.id.action_todayList) ?: return false
        todayListItem.setVisible(sharedPref.getBoolean("pref_conf_today_enable", false))
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_changeLayout) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPref.edit()
            val layoutTypeString = sharedPref.getString("pref_conf_task_layout", "1")
            val layoutType = layoutTypeString!!.toInt()
            editor.putString("pref_conf_task_layout", (layoutType % 2 + 1).toString())
            editor.apply()

            // Update the ViewPagerAdapter to refresh all tabs
            val viewPager = mainFragmentViewPager
            if (viewPager != null) {
                viewPager.adapter!!.notifyDataSetChanged()
            }
            return true
        }
        return false
    }

    /** Called when the user clicks on the Today List button  */
    fun showTodayList() {
        val intent = Intent(this, TodayActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user clicks the Edit Lists button   */
    fun openTaskLists() {
        val fragmentManager = supportFragmentManager

        // Create the fragment
        val taskListFragment = TaskListsDialogFragment.newInstance(
                fragmentManager.findFragmentById(R.id.fragment_main) as MainFragment?)
        taskListFragment.showFragment(fragmentManager,
                getString(R.string.task_list_edit), resources.getBoolean(R.bool.large_layout))
    }

    /** Called when the user clicks the History button */
    fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user clicks the Settings button   */
    fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /** Called when the user clicks the About button   */
    fun openAbout() {
        val fragmentManager = supportFragmentManager

        // Create the fragment
        val taskListFragment = AboutDialogFragment()
        taskListFragment.showFragment(fragmentManager,
                getString(R.string.action_about), resources.getBoolean(R.bool.large_layout))
    }

    /** Called when user clicks on the New Task floating button  */
    fun onNewTaskClick() {
        val viewPager = mainFragmentViewPager ?: return
        val currentTabPosition = viewPager.currentItem
        val pagerAdapter = (viewPager.adapter as SectionsPagerAdapter?)!!
        val taskDialogFragment = TaskFormDialogFragment.newInstance(null,
                pagerAdapter.allItems,
                pagerAdapter.createFragment(currentTabPosition) as TasksFragment)

        // Set some configuration values for the tab
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val args = Bundle()
        args.putInt("list", currentTabPosition)
        args.putBoolean("today", sharedPref.getBoolean("pref_conf_today_enable", false))
        args.putInt("button_count", 2)
        args.putString("button_positive", getString(R.string.new_task_save))
        args.putString("button_negative", getString(R.string.new_task_cancel))
        args.putString("button_neutral", getString(R.string.new_task_delete))
        taskDialogFragment.arguments = args
        val fragmentManager = supportFragmentManager
        taskDialogFragment.showFragment(fragmentManager,
                getString(R.string.action_new_task), resources.getBoolean(R.bool.large_layout))
    }

    private val mainFragmentViewPager: ViewPager2?
        get() {
            val manager = supportFragmentManager
            val fragment = manager.findFragmentById(R.id.fragment_main) as MainFragment?
            return fragment?.viewPager
        }
}
