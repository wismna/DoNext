package com.wismna.geoffroy.donext.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import java.util.List;

/**
 * Created by bg45 on 2017-03-22.
 * Custom array adapter for the Today Task list view
 */

public class TodayArrayAdapter extends ArrayAdapter<Task> {
    public TodayArrayAdapter(@NonNull Context context, @NonNull List<Task> objects) {
        super(context, R.layout.list_task_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_task_item, parent, false);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.task_list_item_title);
        Task item = this.getItem(position);
        if (item != null) {
            titleView.setText(item.getName());
            if (item.isToday()) {
                titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
                titleView.setBackgroundColor(Color.parseColor("#B2DFDB"));
            } else {
                titleView.setTypeface(Typeface.DEFAULT);
                titleView.setBackgroundColor(Color.WHITE);
            }
        }
        return convertView;
    }
}
