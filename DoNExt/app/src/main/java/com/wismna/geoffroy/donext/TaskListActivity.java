package com.wismna.geoffroy.donext;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class TaskListActivity extends ListActivity {
    private TasksDataAccess dataAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        ListView listView = (ListView) findViewById(android.R.id.list);
        dataAccess = new TasksDataAccess(this);
        dataAccess.open();

        //List<TaskList> values = dataAccess.getAllTaskLists();

        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        /*ArrayAdapter<TaskList> adapter = new ArrayAdapter<TaskList>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);*/

        TaskListCursorAdapter adapter = new TaskListCursorAdapter(
                this, R.layout.item_task_list, dataAccess.getAllTaskListsCursor(), 0);
        listView.setAdapter(adapter);
    }

    // Will be called when the create Task List button is clicked
    public void onCreateTaskList(View view) {
        @SuppressWarnings("unchecked")
        //ArrayAdapter<TaskList> adapter = (ArrayAdapter<TaskList>) getListAdapter();
        TaskListCursorAdapter adapter = (TaskListCursorAdapter) getListAdapter();
        TaskList taskList;

        EditText editText = (EditText) findViewById(R.id.new_task_list_name);
        // save the new comment to the database
        taskList = dataAccess.createTaskList(editText.getText().toString());
        //adapter.add(taskList);

        adapter.notifyDataSetChanged();
    }

    public void onDeleteTaskList(View view) {
        @SuppressWarnings("unchecked")
        //ArrayAdapter<TaskList> adapter = (ArrayAdapter<TaskList>) getListAdapter();
        TaskListCursorAdapter adapter = (TaskListCursorAdapter) getListAdapter();
        TaskList taskList;

        if (adapter.getCount() > 0) {
            taskList = (TaskList) getListAdapter().getItem(0);
            dataAccess.deleteTaskList(taskList);
            //adapter.remove(taskList);
        }
        adapter.notifyDataSetChanged();
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
}
