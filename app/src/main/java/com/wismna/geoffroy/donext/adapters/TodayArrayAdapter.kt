package com.wismna.geoffroy.donext.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.dao.Task

/**
 * Created by bg45 on 2017-03-22.
 * Custom array adapter for the Today Task list view
 */
class TodayArrayAdapter(context: Context, objects: List<Task?>) : ArrayAdapter<Task?>(context, R.layout.list_task_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //return super.getView(position, convertView, parent);
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.context).inflate(R.layout.list_task_item, parent, false)
        }
        val titleView = convertView!!.findViewById<TextView>(R.id.task_list_item_title)
        val taskView = convertView.findViewById<TextView>(R.id.task_list_item_tasklist)
        val layoutView = convertView.findViewById<LinearLayout>(R.id.task_list_item_layout)
        val item = getItem(position)
        if (item != null) {
            titleView.text = item.name
            taskView.text = item.taskListName
            if (item.isToday) {
                titleView.setTypeface(titleView.typeface, Typeface.BOLD)
                layoutView.setBackgroundColor(Color.parseColor("#B2DFDB"))
            } else {
                titleView.typeface = Typeface.DEFAULT
                layoutView.setBackgroundColor(Color.WHITE)
            }
        }
        return convertView
    }
}
