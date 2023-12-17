package com.wismna.geoffroy.donext.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wismna.geoffroy.donext.dao.TaskList
import com.wismna.geoffroy.donext.fragments.TasksFragment

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(fragment: Fragment?, /*@Override
    public CharSequence getPageTitle(int position) {
        if (taskLists == null) return "N/A";
        return taskLists.get(position).getName();
    }*/val allItems: List<TaskList>?) : FragmentStateAdapter(fragment!!) {

    override fun createFragment(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        val taskList = allItems!![position]
        return TasksFragment.newTaskListInstance(taskList.id)
    }

    override fun getItemCount(): Int {
        return if (allItems != null) {
            // Show the task lists
            allItems.size
        } else 0
    }
}