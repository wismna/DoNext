package com.wismna.geoffroy.donext.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by geoffroy on 15-11-25.
 * Database helper class that contains table and column names as well as handles database creation
 */
internal class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TASKLIST_TABLE_CREATE)
        db.execSQL(TASKS_TABLE_CREATE)
        db.execSQL(TASKS_VIEW_TODAY_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Fall-through is intended
        when (oldVersion) {
            1 -> {
                // Add new Task List Order column
                db.execSQL("ALTER TABLE " + TASKLIST_TABLE_NAME + " ADD COLUMN " + COLUMN_ORDER + " INTEGER")
                // Add new Visible column
                db.execSQL("ALTER TABLE " + TASKLIST_TABLE_NAME + " ADD COLUMN " + TASKLIST_COLUMN_VISIBLE + " INTEGER DEFAULT 1")
                // Add new Due Date column
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_DUEDATE + " DATE")
                // Add new Today Date column
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYDATE + " DATE")
                // Create the Today view
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("DROP VIEW " + TASKS_VIEW_TODAY_NAME)
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                // Add new Task Order column
                //db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + COLUMN_ORDER + " INTEGER");
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYORDER + " INTEGER")
            }

            2 -> {
                db.execSQL("ALTER TABLE " + TASKLIST_TABLE_NAME + " ADD COLUMN " + TASKLIST_COLUMN_VISIBLE + " INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_DUEDATE + " DATE")
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYDATE + " DATE")
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("DROP VIEW " + TASKS_VIEW_TODAY_NAME)
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYORDER + " INTEGER")
            }

            3 -> {
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYDATE + " DATE")
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("DROP VIEW " + TASKS_VIEW_TODAY_NAME)
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYORDER + " INTEGER")
            }

            4 -> {
                db.execSQL("DROP VIEW " + TASKS_VIEW_TODAY_NAME)
                db.execSQL(TASKS_VIEW_TODAY_CREATE)
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYORDER + " INTEGER")
            }

            5 -> db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYORDER + " INTEGER")
        }
    }

    companion object {
        private const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "donext.db"
        const val COLUMN_ID = "_id"
        const val COLUMN_ORDER = "displayorder"
        const val TASKLIST_TABLE_NAME = "tasklist"
        const val TASKLIST_COLUMN_NAME = "name"
        const val TASKLIST_COLUMN_TASK_COUNT = "taskcount"
        const val TASKLIST_COLUMN_VISIBLE = "visible"
        private const val TASKLIST_TABLE_CREATE = "CREATE TABLE " + TASKLIST_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASKLIST_COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_ORDER + " INTEGER, " +
                TASKLIST_COLUMN_VISIBLE + " INTEGER DEFAULT 1" +
                ");"
        const val TASKS_TABLE_NAME = "tasks"
        const val TASKS_COLUMN_NAME = "name"
        const val TASKS_COLUMN_DESC = "description"
        const val TASKS_COLUMN_CYCLE = "cycle"
        const val TASKS_COLUMN_PRIORITY = "priority"
        const val TASKS_COLUMN_DONE = "done"
        const val TASKS_COLUMN_DELETED = "deleted"
        const val TASKS_COLUMN_LIST = "list"
        const val TASKS_COLUMN_DUEDATE = "duedate"
        const val TASKS_COLUMN_TODAYDATE = "todaydate"
        const val TASKS_COLUMN_TODAYORDER = "todayorder"
        private const val TASKS_TABLE_CREATE = "CREATE TABLE " + TASKS_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASKS_COLUMN_NAME + " TEXT NOT NULL, " +
                TASKS_COLUMN_DESC + " TEXT, " +
                TASKS_COLUMN_PRIORITY + " INTEGER, " +
                TASKS_COLUMN_CYCLE + " INTEGER DEFAULT 0, " +
                TASKS_COLUMN_DONE + " INTEGER DEFAULT 0, " +
                TASKS_COLUMN_DELETED + " INTEGER DEFAULT 0, " +
                COLUMN_ORDER + " INTEGER, " +
                TASKS_COLUMN_TODAYORDER + " INTEGER, " +
                TASKS_COLUMN_LIST + " INTEGER NOT NULL " +
                "REFERENCES " + TASKLIST_TABLE_NAME + "(" + COLUMN_ID + "), " +
                TASKS_COLUMN_DUEDATE + " DATE, " +
                TASKS_COLUMN_TODAYDATE + " DATE" +
                ");"
        const val TASKS_VIEW_TODAY_NAME = "today"
        private const val TASKS_VIEW_TODAY_CREATE = "CREATE VIEW " + TASKS_VIEW_TODAY_NAME + " AS" +
                " SELECT * FROM " + TASKS_TABLE_NAME +
                " WHERE " + TASKS_COLUMN_TODAYDATE + " = date('now','localtime')"
    }
}
