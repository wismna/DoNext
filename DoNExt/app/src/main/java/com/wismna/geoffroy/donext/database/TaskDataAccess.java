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
 * Data access class that handles Tasks
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

    /** Adds or update a task in the database */
    public Task createOrUpdateTask(long id, String name, String description, String priority, long taskList) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, name);
        values.put(DatabaseHelper.TASKS_COLUMN_DESC, description);
        values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priorities.indexOf(priority));
        //values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priority);
        values.put(DatabaseHelper.TASKS_COLUMN_LIST, taskList);
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

    public void deleteTask(long taskId) {
        database.delete(DatabaseHelper.TASKS_TABLE_NAME,
                DatabaseHelper.COLUMN_ID + " = " + taskId, null);
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

    public int getTaskCount(long id) {
        int taskCount;
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) " +
                        " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                        " WHERE " + DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                        " AND " + DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                        " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0, null);
        cursor.moveToFirst();
        taskCount = cursor.getInt(0);
        cursor.close();
        return taskCount;
    }
    public int getTotalCycles(long id) {
        int totalCycles;
        Cursor cursor = database.rawQuery(
                "SELECT SUM(" + DatabaseHelper.TASKS_COLUMN_CYCLE + ") " +
                    " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                    " WHERE " + DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0, null);
        cursor.moveToFirst();
        totalCycles = cursor.getInt(0);
        cursor.close();
        return totalCycles;
    }

    public Cursor getAllTasksCursor(long id) {
        return database.query(DatabaseHelper.TASKS_TABLE_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                    " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0,
                null, null, null,
                DatabaseHelper.TASKS_COLUMN_CYCLE + ", " + DatabaseHelper.COLUMN_ID + " DESC");
    }

    public int setDone(long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_DONE, 1);
        return database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    public int increaseCycle(int newCycle, long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_CYCLE, newCycle);
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
        return task;
    }
}
