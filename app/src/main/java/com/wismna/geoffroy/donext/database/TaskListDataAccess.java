package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wismna.geoffroy.donext.dao.TaskList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geoffroy on 15-11-25.
 * Data access class that handles Task Lists
 */
public class TaskListDataAccess implements AutoCloseable {
    public enum MODE {
        READ,
        WRITE
    }
    // Database fields
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;
    private final static String[] taskListColumns =
            {DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKLIST_COLUMN_NAME,
            DatabaseHelper.COLUMN_ORDER, DatabaseHelper.TASKLIST_COLUMN_VISIBLE};

    public TaskListDataAccess(Context context) {
        this(context, MODE.READ);
    }
    public TaskListDataAccess(Context context, MODE writeMode) {
        dbHelper = new DatabaseHelper(context);
        open(writeMode);
    }

    public void open(MODE writeMode) {
        if (writeMode == MODE.WRITE) database = dbHelper.getWritableDatabase();
        else database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public TaskList createTaskList(String name, int order) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKLIST_COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_ORDER, order);
        values.put(DatabaseHelper.TASKLIST_COLUMN_VISIBLE, 1);
        long insertId = database.insert(DatabaseHelper.TASKLIST_TABLE_NAME, null,
                values);
        Cursor cursor = database.query(DatabaseHelper.TASKLIST_TABLE_NAME,
                taskListColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        TaskList newTaskList = cursorToTaskList(cursor);
        cursor.close();
        return newTaskList;
    }

    public void deleteTaskList(long id) {
        // Mark all tasks as deleted
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_DELETED, 1);
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.TASKS_COLUMN_LIST
                + " = " + id, null);
        // Hide list
        update(id, DatabaseHelper.TASKLIST_COLUMN_VISIBLE, 0);
    }

    public void updateOrder(long id, int order) {
        update(id, DatabaseHelper.COLUMN_ORDER, order);
    }

    public void updateName(long id, String name) {
        update(id, DatabaseHelper.TASKLIST_COLUMN_NAME, name);
    }

    public List<TaskList> getTaskLists(boolean showInvisible) {
        List<TaskList> taskLists = new ArrayList<>();

        Cursor cursor = showInvisible ? getInvisibleTaskListsCursor() : getVisibleTaskListsCursor();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TaskList taskList = cursorToTaskList(cursor);
            taskLists.add(taskList);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return taskLists;
    }

    private void update(long id, String column, Object value)
    {
        ContentValues contentValues = new ContentValues();
        if (value instanceof String)
            contentValues.put(column, (String) value);
        if (value instanceof Integer)
            contentValues.put(column, (int) value);
        database.update(DatabaseHelper.TASKLIST_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    private Cursor getVisibleTaskListsCursor() {
        return database.rawQuery("SELECT *," +
                " (SELECT COUNT(*) " +
                    " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                    " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST + " = " +
                      DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID + ") AS " + DatabaseHelper.TASKLIST_COLUMN_TASK_COUNT +
                " FROM " + DatabaseHelper.TASKLIST_TABLE_NAME +
                " WHERE " + DatabaseHelper.TASKLIST_COLUMN_VISIBLE + " = 1" +
                " ORDER BY " + DatabaseHelper.COLUMN_ORDER + " ASC ",
                null);
    }

    private Cursor getInvisibleTaskListsCursor() {
        return database.rawQuery("SELECT *," +
                " (SELECT COUNT(*) " +
                    " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                    " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST + " = " +
                        DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID +
                        " AND (" + DatabaseHelper.TASKS_COLUMN_DELETED + " = 1" +
                        " OR " + DatabaseHelper.TASKS_COLUMN_DONE + " = 1)" +
                ") AS " + DatabaseHelper.TASKLIST_COLUMN_TASK_COUNT +
                " FROM " + DatabaseHelper.TASKLIST_TABLE_NAME +
                " WHERE " + DatabaseHelper.TASKLIST_COLUMN_VISIBLE + " = 0" +
                    " OR " + DatabaseHelper.TASKLIST_COLUMN_TASK_COUNT + " > 0" +
                " ORDER BY " + DatabaseHelper.COLUMN_ORDER + " ASC ",
                null);
    }

    private TaskList cursorToTaskList(Cursor cursor) {
        TaskList taskList = new TaskList();
        taskList.setId(cursor.getLong(0));
        taskList.setName(cursor.getString(1));
        // Get "false" count column if it exists
        if (cursor.getColumnCount() == 5)
            taskList.setTaskCount(cursor.getLong(4));
        return taskList;
    }

}
