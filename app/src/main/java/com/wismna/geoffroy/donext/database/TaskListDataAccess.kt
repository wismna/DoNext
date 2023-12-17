package com.wismna.geoffroy.donext.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.wismna.geoffroy.donext.dao.TaskList

/**
 * Created by geoffroy on 15-11-25.
 * Data access class that handles Task Lists
 */
class TaskListDataAccess @JvmOverloads constructor(context: Context?, writeMode: MODE = MODE.READ) : AutoCloseable {
    enum class MODE {
        READ,
        WRITE
    }

    // Database fields
    private var database: SQLiteDatabase? = null
    private val dbHelper: DatabaseHelper

    init {
        dbHelper = DatabaseHelper(context)
        open(writeMode)
    }

    fun open(writeMode: MODE) {
        database = if (writeMode == MODE.WRITE) dbHelper.writableDatabase else dbHelper.readableDatabase
    }

    override fun close() {
        dbHelper.close()
    }

    fun createTaskList(name: String?, order: Int): TaskList {
        val values = ContentValues()
        values.put(DatabaseHelper.TASKLIST_COLUMN_NAME, name)
        values.put(DatabaseHelper.COLUMN_ORDER, order)
        values.put(DatabaseHelper.TASKLIST_COLUMN_VISIBLE, 1)
        val insertId = database!!.insert(DatabaseHelper.TASKLIST_TABLE_NAME, null,
                values)
        val cursor = database!!.query(DatabaseHelper.TASKLIST_TABLE_NAME,
                taskListColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null)
        cursor.moveToFirst()
        val newTaskList = cursorToTaskList(cursor)
        cursor.close()
        return newTaskList
    }

    fun deleteTaskList(id: Long) {
        // Mark all tasks as deleted
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.TASKS_COLUMN_DELETED, 1)
        database!!.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.TASKS_COLUMN_LIST
                + " = " + id, null)
        // Hide list
        update(id, DatabaseHelper.TASKLIST_COLUMN_VISIBLE, 0)
    }

    fun updateOrder(id: Long, order: Int) {
        update(id, DatabaseHelper.COLUMN_ORDER, order)
    }

    fun updateName(id: Long, name: String) {
        update(id, DatabaseHelper.TASKLIST_COLUMN_NAME, name)
    }

    fun getTaskLists(showInvisible: Boolean): MutableList<TaskList> {
        val taskLists: MutableList<TaskList> = ArrayList()
        val cursor = if (showInvisible) invisibleTaskListsCursor else visibleTaskListsCursor
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val taskList = cursorToTaskList(cursor)
            taskLists.add(taskList)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return taskLists
    }

    private fun update(id: Long, column: String, value: Any) {
        val contentValues = ContentValues()
        if (value is String) contentValues.put(column, value)
        if (value is Int) contentValues.put(column, value)
        database!!.update(DatabaseHelper.TASKLIST_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null)
    }

    private val visibleTaskListsCursor: Cursor
        get() = database!!.rawQuery("SELECT *," +
                " (SELECT COUNT(*) " +
                " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST + " = " +
                DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID + ") AS " + DatabaseHelper.TASKLIST_COLUMN_TASK_COUNT +
                " FROM " + DatabaseHelper.TASKLIST_TABLE_NAME +
                " WHERE " + DatabaseHelper.TASKLIST_COLUMN_VISIBLE + " = 1" +
                " ORDER BY " + DatabaseHelper.COLUMN_ORDER + " ASC ",
                null)
    private val invisibleTaskListsCursor: Cursor
        get() = database!!.rawQuery("SELECT *," +
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
                null)

    private fun cursorToTaskList(cursor: Cursor): TaskList {
        val taskList = TaskList()
        taskList.id = cursor.getLong(0)
        taskList.name = cursor.getString(1)
        // Get "false" count column if it exists
        if (cursor.columnCount == 5) taskList.taskCount = cursor.getLong(4)
        return taskList
    }

    companion object {
        private val taskListColumns = arrayOf<String>(DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKLIST_COLUMN_NAME,
                DatabaseHelper.COLUMN_ORDER, DatabaseHelper.TASKLIST_COLUMN_VISIBLE)
    }
}
