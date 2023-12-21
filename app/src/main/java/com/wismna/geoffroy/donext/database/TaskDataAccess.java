package com.wismna.geoffroy.donext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
    private final static String[] taskColumns = {
            DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKS_COLUMN_NAME,
            DatabaseHelper.TASKS_COLUMN_DESC, DatabaseHelper.TASKS_COLUMN_PRIORITY,
            DatabaseHelper.TASKS_COLUMN_CYCLE, DatabaseHelper.TASKS_COLUMN_DONE,
            DatabaseHelper.TASKS_COLUMN_DELETED, DatabaseHelper.TASKS_COLUMN_LIST,
            DatabaseHelper.TASKS_COLUMN_DUEDATE, DatabaseHelper.TASKS_COLUMN_TODAYDATE,
            DatabaseHelper.COLUMN_ORDER, DatabaseHelper.TASKS_COLUMN_TODAYORDER};

    public TaskDataAccess(Context context) {
        this(context, MODE.READ);
    }
    public TaskDataAccess(Context context, MODE writeMode) {
        dbHelper = new DatabaseHelper(context);
        open(writeMode);
    }

    private void open(MODE writeMode) {
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
        values.put(DatabaseHelper.COLUMN_ORDER, getMaxOrder(taskList) + 1);
        long insertId;
        if (id == 0)
            insertId = database.insert(DatabaseHelper.TASKS_TABLE_NAME, null, values);
        else {
            database.update(DatabaseHelper.TASKS_TABLE_NAME, values, DatabaseHelper.COLUMN_ID + " == " + id, null);
            insertId = id;
        }
        Task newTask;
        try (Cursor cursor = database.query(DatabaseHelper.TASKS_TABLE_NAME,
                taskColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null)) {
            cursor.moveToFirst();
            newTask = cursorToTask(cursor);
        }
        return newTask;
    }

    public void updateTodayTasks(long id, boolean isTodayList, int position){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_TODAYDATE, isTodayList? LocalDate.now().toString() : "");
        contentValues.put(DatabaseHelper.TASKS_COLUMN_TODAYORDER, isTodayList? position : 0);
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
                " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DONE + " = 0" +
                     " AND " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DELETED + " = 0"
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
                DatabaseHelper.COLUMN_ORDER);
        List<Task> tasks = getTasksFromCursor(cursor);

        // Update orders for database migration
        int count = tasks.size();
        if (getMaxOrder(id) == 0 && count > 1) {
            for (int i = 1; i < count; i++) {
                update(tasks.get(i).getId(), DatabaseHelper.COLUMN_ORDER, i);
                tasks.get(i).setOrder(i);
            }
        }
        return tasks;
    }

    public List<Task> getTodayTasks() {
        Cursor cursor = database.query(DatabaseHelper.TASKS_VIEW_TODAY_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_DONE + " = 0" +
                        " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = 0",
                null, null, null,
                DatabaseHelper.TASKS_COLUMN_TODAYORDER);
        return getTasksFromCursor(cursor);
    }

    public void increaseCycle(Task task, boolean isToday) {
        String orderColumn = isToday ? DatabaseHelper.TASKS_COLUMN_TODAYORDER : DatabaseHelper.COLUMN_ORDER;
        updateRemainingRowsOrder(task.getId(), orderColumn);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TASKS_COLUMN_CYCLE, task.getCycle());
        contentValues.put(orderColumn, getMaxOrder(task.getTaskListId()) + 1);
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues,
                DatabaseHelper.COLUMN_ID + " == " + task.getId(), null);
    }

    public void setDone(long id, boolean isToday) {
        update(id, DatabaseHelper.TASKS_COLUMN_DONE, 1);
        updateRemainingRowsOrder(id, isToday ? DatabaseHelper.TASKS_COLUMN_TODAYORDER : DatabaseHelper.COLUMN_ORDER);
    }

    public void deleteTask(long id, boolean isToday) {
        update(id, DatabaseHelper.TASKS_COLUMN_DELETED, 1);
        updateRemainingRowsOrder(id, isToday ? DatabaseHelper.TASKS_COLUMN_TODAYORDER : DatabaseHelper.COLUMN_ORDER);
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
        task.setOrder(cursor.getInt(10));
        task.setTodayOrder(cursor.getInt(11));
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

    private int getMaxOrder(long listId) {
        try (Cursor maxOrderCursor = database.query(DatabaseHelper.TASKS_TABLE_NAME, new String[]{"MAX(" + DatabaseHelper.COLUMN_ORDER + ")"},
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + listId, null, null, null, null)) {
            maxOrderCursor.moveToFirst();

            return maxOrderCursor.getInt(0);
        }
    }

    private void updateRemainingRowsOrder(long id, String orderColumn) {
        try (Cursor cursor = database.rawQuery("UPDATE " + DatabaseHelper.TASKS_TABLE_NAME +
            " SET " + orderColumn + " = " + orderColumn + " - 1" +
            " WHERE " + orderColumn + " > " +
                "(SELECT " + orderColumn +
                " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                " WHERE " + DatabaseHelper.COLUMN_ID + " = " + id + ")",
                null)) {
            cursor.moveToFirst();
        }
    }
}
