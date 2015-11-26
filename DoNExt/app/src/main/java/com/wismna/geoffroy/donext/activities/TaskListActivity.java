package com.wismna.geoffroy.donext.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskListCursorAdapter;
import com.wismna.geoffroy.donext.database.TasksDataAccess;

public class TaskListActivity extends AppCompatActivity {
    private TasksDataAccess dataAccess;
    private TaskListCursorAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        listView = (ListView) findViewById(android.R.id.list);
        dataAccess = new TasksDataAccess(this);
        dataAccess.open();

        adapter = new TaskListCursorAdapter(
                this, R.layout.item_task_list, dataAccess.getAllTaskListsCursor(), 0);
        listView.setAdapter(adapter);

        updateCreateButtonEnabled();
    }

    // Will be called when the create Task List button is clicked
    public void onCreateTaskList(View view) {
        @SuppressWarnings("unchecked")
        EditText editText = (EditText) findViewById(R.id.new_task_list_name);

        Cursor cursor = dataAccess.createTaskList(editText.getText().toString());
        adapter.changeCursor(cursor);
        editText.setText("");
        updateCreateButtonEnabled();
    }

    public void onDeleteTaskList(View view) {
        @SuppressWarnings("unchecked")
        final int position = listView.getPositionForView((View) view.getParent());
        Cursor cursor = dataAccess.deleteTaskList((Cursor) adapter.getItem(position));
        adapter.changeCursor(cursor);
        updateCreateButtonEnabled();
    }

    @Override
    protected void onResume() {
        dataAccess.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        dataAccess.close();
        super.onPause();
    }

    private void updateCreateButtonEnabled() {
        //Button createButton = (Button) findViewById(R.id.new_task_list_button);
        //EditText editText = (EditText) findViewById(R.id.new_task_list_name);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.new_task_list_layout);
        int taskListCount = adapter.getCount();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String maxTaskListsString = sharedPref.getString("pref_conf_max_lists", "3");
        int maxTaskLists = Integer.valueOf(maxTaskListsString);
        //createButton.setEnabled(taskListCount < maxTaskLists);
        //editText.setEnabled(taskListCount < maxTaskLists);
        if (taskListCount >= maxTaskLists) layout.setVisibility(View.GONE);
        else layout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataAccess.close();
    }
}
