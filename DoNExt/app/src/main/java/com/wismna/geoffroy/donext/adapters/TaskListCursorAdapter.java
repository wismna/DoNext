package com.wismna.geoffroy.donext.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.database.DatabaseHelper;

/**
 * Created by geoffroy on 15-11-25.
 */
public class TaskListCursorAdapter extends ResourceCursorAdapter {
    public TaskListCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView taskListCount = (TextView) view.findViewById(R.id.task_list_count);
        TextView taskListName = (TextView) view.findViewById(R.id.task_list_name);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TASKLIST_COLUMN_NAME));
        // TODO: Update the count when tasks are implemented
        int count = 0;
        // Populate fields with extracted properties
        taskListCount.setText(String.valueOf(count));
        taskListName.setText(name);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
