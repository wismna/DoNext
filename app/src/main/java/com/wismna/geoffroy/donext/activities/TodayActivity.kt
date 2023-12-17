package com.wismna.geoffroy.donext.activities

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.adapters.TaskRecyclerViewAdapter
import com.wismna.geoffroy.donext.database.TaskDataAccess
import com.wismna.geoffroy.donext.fragments.TodayFormDialogFragment
import com.wismna.geoffroy.donext.fragments.TodayFormDialogFragment.TodayTaskListener
import org.joda.time.LocalDate
import java.util.Locale
import java.util.Objects

class TodayActivity : AppCompatActivity(), TodayTaskListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // Show the date
        val date = findViewById<TextView>(R.id.today_date)
        date.text = LocalDate.now().toString("EEEE, dd MMMM yyyy", currentLocale)

        // Set the no tasks texts
        /*TextView noTasks = findViewById(R.id.no_more_tasks);
        noTasks.setText(R.string.today_no_tasks);
        noTasks.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_smiley_satisfied_light, 0);
        noTasks.setCompoundDrawablePadding(10);

        TextView createTasks = findViewById(R.id.create_tasks);
        createTasks.setText(R.string.today_create_tasks);*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_today, menu)
        return true
    }

    override fun onTodayTaskDialogPositiveClick(dialogView: View?) {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.isEnabled = false
    }

    override fun onTodayTasksUpdated() {
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.isEnabled = true
        TaskDataAccess(this).use { taskDataAccess ->
            val recyclerView = findViewById<RecyclerView>(R.id.task_list_view)
            (Objects.requireNonNull(recyclerView.adapter) as TaskRecyclerViewAdapter).setItems(taskDataAccess.todayTasks)
        }
    }

    fun onNewTaskClick(view: View?) {
        val taskDialogFragment = TodayFormDialogFragment.newInstance(this@TodayActivity)
        val fragmentManager = supportFragmentManager
        taskDialogFragment.showFragment(fragmentManager, getString(R.string.action_today_select), resources.getBoolean(R.bool.large_layout))
    }

    private val currentLocale: Locale
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
}
