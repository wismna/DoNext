package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geoffroy on 15-11-27.
 */
public class TaskDataAccess {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] taskColumns = {
            DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKS_COLUMN_NAME,
            DatabaseHelper.TASKS_COLUMN_DESC, DatabaseHelper.TASKS_COLUMN_PRIORITY,
            DatabaseHelper.TASKS_COLUMN_CYCLE, DatabaseHelper.TASKS_COLUMN_DONE,
            DatabaseHelper.TASKS_COLUMN_DELETED, DatabaseHelper.TASKS_COLUMN_LIST};
    private List<String> priorities = new ArrayList<>();

    public TaskDataAccess(Context context) {
        dbHelper = new DatabaseHelper(context);

        priorities.add(context.getString(R.string.new_task_priority_low));
        priorities.add(context.getString(R.string.new_task_priority_normal));
        priorities.add(context.getString(R.string.new_task_priority_high));
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    public Cursor createTask(String name, String description, String priority, long taskList) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, name);
        values.put(DatabaseHelper.TASKS_COLUMN_DESC, description);
        values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priorities.indexOf(priority));
        values.put(DatabaseHelper.TASKS_COLUMN_LIST, taskList);
        database.insert(DatabaseHelper.TASKS_TABLE_NAME, null, values);
        return getAllTasksCursor();
    }

    public Cursor deleteTask(Cursor taskCursor) {
        Task task = cursorToTask(taskCursor);
        long id = task.getId();
        //System.out.println("Task deleted with id: " + id);
        database.delete(DatabaseHelper.TASKS_TABLE_NAME, DatabaseHelper.COLUMN_ID
                + " = " + id, null);
        return getAllTasksCursor();
    }

    public List<Task> getAllTasks(long id) {
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = id != -1 ? getAllTasksCursor(id) : getAllTasksCursor();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tasks;
    }

    public Cursor getAllTasksCursor() {
        return database.query(DatabaseHelper.TASKS_TABLE_NAME,
                taskColumns, null, null, null, null, null);
    }

    public Cursor getAllTasksCursor(long id) {
        return database.query(DatabaseHelper.TASKS_TABLE_NAME,
                taskColumns, DatabaseHelper.TASKS_COLUMN_LIST + " = " + id, null, null, null, null);
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(0));
        task.setName(cursor.getString(1));
        task.setDescription(cursor.getString(2));
        task.setPriority(cursor.getInt(3));
        task.setCycle(cursor.getInt(4));
        task.setDone(cursor.getInt(5));
        task.setDeleted(cursor.getInt(6));
        task.setTaskList(cursor.getLong(7));
        return task;
    }
}
