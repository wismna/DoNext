package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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
    private DatabaseHelper dbHelper;
    private String[] taskListColumns =
            {DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKLIST_COLUMN_NAME,
            DatabaseHelper.COLUMN_ORDER, DatabaseHelper.TASKLIST_COLUMN_VISIBLE};

    public TaskListDataAccess(Context context) {
        this(context, MODE.READ);
    }
    public TaskListDataAccess(Context context, MODE writeMode) {
        dbHelper = new DatabaseHelper(context);
        open(writeMode);
    }

    public void open(MODE writeMode) throws SQLException {
        if (writeMode == MODE.WRITE) database = dbHelper.getWritableDatabase();
        else database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public TaskList createTaskList(String name, int order) {
        return createTaskList(name, order, true);
    }

    public TaskList createTaskList(String name, int order, Boolean visible) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKLIST_COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_ORDER, order);
        values.put(DatabaseHelper.TASKLIST_COLUMN_VISIBLE, visible ? 1 : 0);
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
        // Delete all related tasks
        database.delete(DatabaseHelper.TASKS_TABLE_NAME, DatabaseHelper.TASKS_COLUMN_LIST
                + " = " + id, null);
        // Delete list
        database.delete(DatabaseHelper.TASKLIST_TABLE_NAME, DatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void updateOrder(long id, int order) {
        update(id, DatabaseHelper.COLUMN_ORDER, order);
    }

    public void updateName(long id, String name) {
        update(id, DatabaseHelper.TASKLIST_COLUMN_NAME, name);
    }

    public void updateVisibility(long id, boolean visible){
        update(id, DatabaseHelper.TASKLIST_COLUMN_VISIBLE, visible ? 1 : 0);
    }

    public TaskList getTaskListByName(String name) {
        Cursor cursor = getTaskListByNameCursor(name);
        TaskList taskList = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            taskList = cursorToTaskList(cursor);
            cursor.close();
        }
        return taskList;
    }

    public List<TaskList> getAllTaskLists() {
        List<TaskList> taskLists = new ArrayList<>();

        Cursor cursor = getAllTaskListsCursor();
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

    private Cursor getTaskListByNameCursor(String name) {
        return database.query(true, DatabaseHelper.TASKLIST_TABLE_NAME, taskListColumns,
                DatabaseHelper.TASKLIST_COLUMN_NAME + " = '" + name.replace("'", "''") + "'", null, null, null, null, null);
    }
    private Cursor getAllTaskListsCursor() {
        return database.rawQuery("SELECT *," +
                " (SELECT COUNT(*) " +
                    " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                    " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST + " = " +
                      DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID + ") AS " + DatabaseHelper.TASKLIST_COLUMN_TASK_COUNT +
                " FROM " + DatabaseHelper.TASKLIST_TABLE_NAME +
                " WHERE " + DatabaseHelper.TASKLIST_COLUMN_VISIBLE + " = " + 1 +
                " ORDER BY " + DatabaseHelper.COLUMN_ORDER + " ASC ",
                null);
    }

    private TaskList cursorToTaskList(Cursor cursor) {
        TaskList taskList = new TaskList();
        taskList.setId(cursor.getLong(0));
        taskList.setName(cursor.getString(1));
        taskList.setOrder(cursor.getInt(2));
        taskList.setVisible(cursor.getInt(3));
        // Get "false" count column if it exists
        if (cursor.getColumnCount() == 5)
            taskList.setTaskCount(cursor.getLong(4));
        return taskList;
    }

}
