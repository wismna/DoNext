package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geoffroy on 15-11-27.
 * Data access class that handles Tasks
 */
public class TaskDataAccess implements AutoCloseable {
    public enum MODE {
        READ,
        WRITE
    }

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] taskColumns = {
            DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKS_COLUMN_NAME,
            DatabaseHelper.TASKS_COLUMN_DESC, DatabaseHelper.TASKS_COLUMN_PRIORITY,
            DatabaseHelper.TASKS_COLUMN_CYCLE, DatabaseHelper.TASKS_COLUMN_DONE,
            DatabaseHelper.TASKS_COLUMN_DELETED, DatabaseHelper.TASKS_COLUMN_LIST};
    private List<String> priorities = new ArrayList<>();

    public TaskDataAccess(Context context) {
        this(context, MODE.READ);
    }
    public TaskDataAccess(Context context, MODE writeMode) {
        dbHelper = new DatabaseHelper(context);

        priorities.add(context.getString(R.string.new_task_priority_low));
        priorities.add(context.getString(R.string.new_task_priority_normal));
        priorities.add(context.getString(R.string.new_task_priority_high));

        open(writeMode);
    }

    public void open(MODE writeMode) throws SQLException {
        if (writeMode == MODE.WRITE) database = dbHelper.getWritableDatabase();
        else database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /** Adds or update a task in the database */
    public Task createOrUpdateTask(long id, String name, String description, String priority, long taskList) {
        return createOrUpdateTask(id, name, description, priority, taskList, new Date());
    }
    public Task createOrUpdateTask(long id, String name, String description, String priority, long taskList, Date date) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, name);
        values.put(DatabaseHelper.TASKS_COLUMN_DESC, description);
        values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priorities.indexOf(priority));
        values.put(DatabaseHelper.TASKS_COLUMN_LIST, taskList);
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        String dateString = sdf.format(date);
        values.put(DatabaseHelper.TASKS_COLUMN_DUEDATE, dateString);
        long insertId;
        if (id == 0)
            insertId = database.insert(DatabaseHelper.TASKS_TABLE_NAME, null, values);
        else {
            database.update(DatabaseHelper.TASKS_TABLE_NAME, values, DatabaseHelper.COLUMN_ID + " == " + id, null);
            insertId = id;
        }
        Cursor cursor = database.query(DatabaseHelper.TASKS_TABLE_NAME,
                taskColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Task newTask = cursorToTask(cursor);
        cursor.close();
        return newTask;
    }

    public List<Task> getAllTasks(long id) {
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = getAllTasksCursor(id);

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

    private Cursor getAllTasksCursor(long id) {
        return database.query(DatabaseHelper.TASKS_TABLE_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0,
                null, null, null,
                DatabaseHelper.TASKS_COLUMN_CYCLE + ", " + DatabaseHelper.COLUMN_ID + " DESC");
    }

    public int setDone(long id) {
        return update(id, DatabaseHelper.TASKS_COLUMN_DONE, 1);
    }

    public int increaseCycle(int newCycle, long id) {
        return update(id, DatabaseHelper.TASKS_COLUMN_CYCLE, newCycle);
    }

    public int deleteTask(long id) {
        /*database.delete(DatabaseHelper.TASKS_TABLE_NAME,
                DatabaseHelper.COLUMN_ID + " = " + taskId, null);*/
        return update(id, DatabaseHelper.TASKS_COLUMN_DELETED, 1);
    }

    private int update(long id, String column, Object value) {
        ContentValues contentValues = new ContentValues();
        if (value instanceof Integer)
            contentValues.put(column, (int) value);
        return database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null);
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
        task.setDueDate(cursor.getString(8));
        return task;
    }
}
