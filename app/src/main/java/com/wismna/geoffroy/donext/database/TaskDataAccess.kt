package com.wismna.geoffroy.donext.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.wismna.geoffroy.donext.dao.Task
import org.joda.time.LocalDate

/**
 * Created by geoffroy on 15-11-27.
 * Data access class that handles Tasks
 */
class TaskDataAccess @JvmOverloads constructor(context: Context?, writeMode: MODE = MODE.READ) : AutoCloseable {
    enum class MODE {
        READ,
        WRITE
    }

    private var database: SQLiteDatabase? = null
    private val dbHelper: DatabaseHelper

    init {
        dbHelper = DatabaseHelper(context)
        open(writeMode)
    }

    private fun open(writeMode: MODE) {
        database = if (writeMode == MODE.WRITE) dbHelper.writableDatabase else dbHelper.readableDatabase
    }

    override fun close() {
        dbHelper.close()
    }

    /** Adds or update a task in the database  */
    fun createOrUpdateTask(id: Long, name: String?, description: String?, priority: Int,
                           taskList: Long, dueDate: String?, isTodayList: Boolean): Task {
        val values = ContentValues()
        values.put(DatabaseHelper.TASKS_COLUMN_NAME, name)
        values.put(DatabaseHelper.TASKS_COLUMN_DESC, description)
        values.put(DatabaseHelper.TASKS_COLUMN_PRIORITY, priority)
        values.put(DatabaseHelper.TASKS_COLUMN_LIST, taskList)
        values.put(DatabaseHelper.TASKS_COLUMN_DUEDATE, dueDate)
        values.put(DatabaseHelper.TASKS_COLUMN_TODAYDATE, if (isTodayList) LocalDate.now().toString() else "")
        values.put(DatabaseHelper.COLUMN_ORDER, getMaxOrder(taskList) + 1)
        val insertId: Long = if (id == 0L) database!!.insert(DatabaseHelper.TASKS_TABLE_NAME, null, values) else {
            database!!.update(DatabaseHelper.TASKS_TABLE_NAME, values, DatabaseHelper.COLUMN_ID + " == " + id, null)
            id
        }
        var newTask: Task
        database!!.query(DatabaseHelper.TASKS_TABLE_NAME,
                taskColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null).use { cursor ->
            cursor.moveToFirst()
            newTask = cursorToTask(cursor)
        }
        return newTask
    }

    fun updateTodayTasks(id: Long, isTodayList: Boolean, position: Int) {
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.TASKS_COLUMN_TODAYDATE, if (isTodayList) LocalDate.now().toString() else "")
        contentValues.put(DatabaseHelper.TASKS_COLUMN_TODAYORDER, if (isTodayList) position else 0)
        database!!.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues,
                DatabaseHelper.COLUMN_ID + " == " + id, null)
    }

    val allTasks: List<Task>
        get() {
            val cursor = database!!.rawQuery("SELECT " +
                    DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID + "," +
                    DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_NAME + "," +
                    DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_TODAYDATE + "," +
                    DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.TASKLIST_COLUMN_NAME + " AS tasklistname " +
                    " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                    " LEFT JOIN " + DatabaseHelper.TASKLIST_TABLE_NAME +
                    " ON " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_LIST +
                    " = " + DatabaseHelper.TASKLIST_TABLE_NAME + "." + DatabaseHelper.COLUMN_ID +
                    " WHERE " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DONE + " = 0" +
                    " AND " + DatabaseHelper.TASKS_TABLE_NAME + "." + DatabaseHelper.TASKS_COLUMN_DELETED + " = 0", null)
            val tasks: MutableList<Task> = ArrayList()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val task = Task()
                task.id = cursor.getLong(0)
                task.name = cursor.getString(1)
                task.setTodayDate(cursor.getString(2))
                task.taskListName = cursor.getString(3)
                tasks.add(task)
                cursor.moveToNext()
            }
            // make sure to close the cursor
            cursor.close()
            return tasks
        }

    fun getAllTasksFromList(id: Long, isHistory: Boolean): MutableList<Task> {

        // REMOVE THIS
        /*ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_ORDER, 0);
        database.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues,
                null, null);*/
        // --
        val history = if (isHistory) 1 else 0
        val cursor = database!!.query(DatabaseHelper.TASKS_TABLE_NAME, taskColumns,
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + id +
                        " AND (" + DatabaseHelper.TASKS_COLUMN_DONE + " = " + history +
                        (if (isHistory) " OR " else " AND ") + DatabaseHelper.TASKS_COLUMN_DELETED + " = " + history + ")",
                null, null, null,
                DatabaseHelper.COLUMN_ORDER)
        val tasks = getTasksFromCursor(cursor)

        // Update orders for database migration
        val count = tasks.size
        if (getMaxOrder(id) == 0 && count > 1) {
            for (i in 1 until count) {
                update(tasks[i].id, DatabaseHelper.COLUMN_ORDER, i)
                tasks[i].order = i
            }
        }
        return tasks
    }

    val todayTasks: MutableList<Task>
        get() {
            val cursor = database!!.query(DatabaseHelper.TASKS_VIEW_TODAY_NAME, taskColumns,
                    DatabaseHelper.TASKS_COLUMN_DONE + " = 0" +
                            " AND " + DatabaseHelper.TASKS_COLUMN_DELETED + " = 0",
                    null, null, null,
                    DatabaseHelper.TASKS_COLUMN_TODAYORDER)
            return getTasksFromCursor(cursor).toMutableList()
        }

    fun increaseCycle(task: Task, isToday: Boolean) {
        val orderColumn: String = if (isToday) DatabaseHelper.TASKS_COLUMN_TODAYORDER else DatabaseHelper.COLUMN_ORDER
        updateRemainingRowsOrder(task.id, orderColumn)
        val contentValues = ContentValues()
        contentValues.put(DatabaseHelper.TASKS_COLUMN_CYCLE, task.cycle)
        contentValues.put(orderColumn, getMaxOrder(task.taskListId) + 1)
        database!!.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues,
                DatabaseHelper.COLUMN_ID + " == " + task.id, null)
    }

    fun setDone(id: Long, isToday: Boolean) {
        update(id, DatabaseHelper.TASKS_COLUMN_DONE, 1)
        updateRemainingRowsOrder(id, if (isToday) DatabaseHelper.TASKS_COLUMN_TODAYORDER else DatabaseHelper.COLUMN_ORDER)
    }

    fun deleteTask(id: Long, isToday: Boolean) {
        update(id, DatabaseHelper.TASKS_COLUMN_DELETED, 1)
        updateRemainingRowsOrder(id, if (isToday) DatabaseHelper.TASKS_COLUMN_TODAYORDER else DatabaseHelper.COLUMN_ORDER)
    }

    private fun update(id: Long, column: String, value: Any) {
        val contentValues = ContentValues()
        if (value is Int) contentValues.put(column, value)
        database!!.update(DatabaseHelper.TASKS_TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + " = " + id, null)
    }

    private fun cursorToTask(cursor: Cursor): Task {
        val task = Task()
        task.id = cursor.getLong(0)
        task.name = cursor.getString(1)
        task.description = cursor.getString(2)
        task.priority = cursor.getInt(3)
        task.cycle = cursor.getInt(4)
        task.done = cursor.getInt(5)
        task.deleted = cursor.getInt(6)
        task.setTaskList(cursor.getLong(7))
        task.setDueDate(cursor.getString(8))
        task.setTodayDate(cursor.getString(9))
        task.order = cursor.getInt(10)
        task.todayOrder = cursor.getInt(11)
        return task
    }

    private fun getTasksFromCursor(cursor: Cursor): MutableList<Task> {
        val tasks: MutableList<Task> = ArrayList()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val task = cursorToTask(cursor)
            tasks.add(task)
            cursor.moveToNext()
        }
        // make sure to close the cursor
        cursor.close()
        return tasks
    }

    private fun getMaxOrder(listId: Long): Int {
        database!!.query(DatabaseHelper.TASKS_TABLE_NAME, arrayOf<String>("MAX(" + DatabaseHelper.COLUMN_ORDER + ")"),
                DatabaseHelper.TASKS_COLUMN_LIST + " = " + listId, null, null, null, null).use { maxOrderCursor ->
            maxOrderCursor.moveToFirst()
            return maxOrderCursor.getInt(0)
        }
    }

    private fun updateRemainingRowsOrder(id: Long, orderColumn: String) {
        database!!.rawQuery("UPDATE " + DatabaseHelper.TASKS_TABLE_NAME +
                " SET " + orderColumn + " = " + orderColumn + " - 1" +
                " WHERE " + orderColumn + " > " +
                "(SELECT " + orderColumn +
                " FROM " + DatabaseHelper.TASKS_TABLE_NAME +
                " WHERE " + DatabaseHelper.COLUMN_ID + " = " + id + ")",
                null).use { cursor -> cursor.moveToFirst() }
    }

    companion object {
        private val taskColumns = arrayOf<String>(
                DatabaseHelper.COLUMN_ID, DatabaseHelper.TASKS_COLUMN_NAME,
                DatabaseHelper.TASKS_COLUMN_DESC, DatabaseHelper.TASKS_COLUMN_PRIORITY,
                DatabaseHelper.TASKS_COLUMN_CYCLE, DatabaseHelper.TASKS_COLUMN_DONE,
                DatabaseHelper.TASKS_COLUMN_DELETED, DatabaseHelper.TASKS_COLUMN_LIST,
                DatabaseHelper.TASKS_COLUMN_DUEDATE, DatabaseHelper.TASKS_COLUMN_TODAYDATE,
                DatabaseHelper.COLUMN_ORDER, DatabaseHelper.TASKS_COLUMN_TODAYORDER)
    }
}
