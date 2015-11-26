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
 */
public class TasksDataAccess {
    // Database fields
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] taskListColumns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKLIST_COLUMN_NAME};

    public TasksDataAccess(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /*public TaskList createTaskList(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKLIST_COLUMN_NAME, name);
        long insertId = database.insert(DatabaseHelper.TASKLIST_TABLE_NAME, null,
                values);
        Cursor cursor = database.query(DatabaseHelper.TASKLIST_TABLE_NAME,
                taskListColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        TaskList newTaskList = cursorToTaskList(cursor);
        cursor.close();
        return newTaskList;
    }*/

    public Cursor createTaskList(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASKLIST_COLUMN_NAME, name);
        database.insert(DatabaseHelper.TASKLIST_TABLE_NAME, null, values);
        return getAllTaskListsCursor();
    }

    /*public void deleteTaskList(TaskList comment) {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(DatabaseHelper.TASKLIST_TABLE_NAME, DatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }*/

    public Cursor deleteTaskList(Cursor taskListCursor) {
        TaskList taskList = cursorToTaskList(taskListCursor);
        long id = taskList.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(DatabaseHelper.TASKLIST_TABLE_NAME, DatabaseHelper.COLUMN_ID
                + " = " + id, null);
        return getAllTaskListsCursor();
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

    public Cursor getAllTaskListsCursor() {
        return database.query(DatabaseHelper.TASKLIST_TABLE_NAME,
                taskListColumns, null, null, null, null, null);
    }

    private TaskList cursorToTaskList(Cursor cursor) {
        TaskList taskList = new TaskList();
        taskList.setId(cursor.getLong(0));
        taskList.setName(cursor.getString(1));
        return taskList;
    }
}
