package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.wismna.geoffroy.donext.dao.Task;

import org.joda.time.LocalDate;

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
    private final DatabaseHelper dbHelper;
    private final String[] taskColumns = {
            DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKS_COLUMN_NAME,
            DatabaseHelper.TASKS_COLUMN_DESC, DatabaseHelper.TASKS_COLUMN_PRIORITY,
            DatabaseHelper.TASKS_COLUMN_CYCLE, DatabaseHelper.TASKS_COLUMN_DONE,
            DatabaseHelper.TASKS_COLUMN_DELETED, DatabaseHelper.TASKS_COLUMN_LIST,
            DatabaseHelper.TASKS_COLUMN_DUEDATE, DatabaseHelper.TASKS_COLUMN_TODAYDATE};

    public TaskDataAccess(Context context) {
        this(context, MODE.READ);
    }
    public TaskDataAccess(Context context, MODE writeMode) {
        dbHelper = new DatabaseHelper(context);
        open(writeMode);
    }

    private void open(MODE writeMode) throws SQLException {
        if (writeMode == MODE.WRITE) database = dbHelper.getWritableDatabase();
        else database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /** Adds or update a task in the database */
    public Task createOrUpdateTask(long id, String name, String description, int priority,
                                   long taskList, String dueDate, boolean isTodayList) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, name);
        values.put(DatabaseHelper.TASKS_COLUMN_DESC, description);
        values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priority);
        values.put(DatabaseHelper.TASKS_COLUMN_LIST, taskList);
        values.put(DatabaseHelper.TASKS_COLUMN_DUEDATE, dueDate);
        values.put(DatabaseHelper.TASKS_COLUMN_TODAYDATE, isTodayList? LocalDate.now().toString() : "");
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

    public void updateTodayTasks(long id, boolean isTodayList){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_TODAYDATE, isTodayList? LocalDate.now().toString() : "");
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues,
                DatabaseHelper.COLUMN_ID + " == " + id, null);
    }

    public List<Task> getAllTasks() {
        Cursor cursor = database.rawQuery("SELECT " +
                DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID + "," +
                DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_NAME + "," +
                DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_TODAYDATE + "," +
                DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.TASKLIST_COLUMN_NAME + " AS tasklistname " +
                " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                " LEFT JOIN " + DatabaseHelper.TASKLIST_TABLE_NAME +
                " ON " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST +
                    " = " + DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID +
                " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                     " AND " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0
                , null);
        List<Task> tasks = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = new Task();
            task.setId(cursor.getLong(0));
            task.setName(cursor.getString(1));
            task.setTodayDate(cursor.getString(2));
            task.setTaskListName(cursor.getString(3));
            tasks.add(task);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return tasks;
    }

    public List<Task> getAllTasksFromList(long id, boolean isHistory) {
        int history = isHistory ? 1 : 0;
        Cursor cursor = database.query(DatabaseHelper.TASKS_TABLE_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                        " AND (" + DatabaseHelper.TASKS_COLUMN_DONE + " = " + history +
                        (isHistory ? " OR " : " AND ") + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + history + ")",
                null, null, null,
                DatabaseHelper.TASKS_COLUMN_CYCLE + ", " + DatabaseHelper.COLUMN_ID + " DESC");
        return getTasksFromCursor(cursor);
    }

    public List<Task> getTodayTasks() {
        Cursor cursor = database.query(DatabaseHelper.TASKS_VIEW_TODAY_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_DONE + " = " + 0 +
                        " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + 0,
                null, null, null,
                DatabaseHelper.TASKS_COLUMN_CYCLE + ", " + DatabaseHelper.COLUMN_ID + " DESC");
        return getTasksFromCursor(cursor);
    }

    public void setDone(long id) {
        update(id, DatabaseHelper.TASKS_COLUMN_DONE, 1);
    }

    public void increaseCycle(int newCycle, long id) {
        update(id, DatabaseHelper.TASKS_COLUMN_CYCLE, newCycle);
    }

    public void deleteTask(long id) {
        /*database.delete(DatabaseHelper.TASKS_TABLE_NAME,
                DatabaseHelper.COLUMN_ID + " = " + taskId, null);*/
        update(id, DatabaseHelper.TASKS_COLUMN_DELETED, 1);
    }

    private void update(long id, String column, Object value) {
        ContentValues contentValues = new ContentValues();
        if (value instanceof Integer)
            contentValues.put(column, (int) value);
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null);
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
        task.setTodayDate(cursor.getString(9));
        return task;
    }

    private List<Task> getTasksFromCursor(Cursor cursor) {
        List<Task> tasks = new ArrayList<>();

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
}
