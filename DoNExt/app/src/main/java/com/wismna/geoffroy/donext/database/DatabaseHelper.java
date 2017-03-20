package com.wismna.geoffroy.donext.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by geoffroy on 15-11-25.
 * Database helper class that contains table and column names as well as handles database creation
 */
class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "donext.db";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_ORDER = "displayorder";

    static final String TASKLIST_TABLE_NAME = "tasklist";
    static final String TASKLIST_COLUMN_NAME = "name";
    static final String TASKLIST_COLUMN_TASK_COUNT = "taskcount";
    static final String TASKLIST_COLUMN_VISIBLE = "visible";
    private static final String TASKLIST_TABLE_CREATE =
        "CREATE TABLE " + TASKLIST_TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TASKLIST_COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_ORDER + " INTEGER, " +
            TASKLIST_COLUMN_VISIBLE + " INTEGER DEFAULT 1" +
        ");";

    static final String TASKS_TABLE_NAME = "tasks";
    static final String TASKS_COLUMN_NAME = "name";
    static final String TASKS_COLUMN_DESC = "description";
    static final String TASKS_COLUMN_CYCLE = "cycle";
    static final String TASKS_COLUMN_PRIORITY = "priority";
    static final String TASKS_COLUMN_DONE = "done";
    static final String TASKS_COLUMN_DELETED= "deleted";
    static final String TASKS_COLUMN_LIST = "list";
    static final String TASKS_COLUMN_DUEDATE = "duedate";
    static final String TASKS_COLUMN_TODAYDATE = "todaydate";
    private static final String TASKS_TABLE_CREATE =
        "CREATE TABLE " + TASKS_TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TASKS_COLUMN_NAME + " TEXT NOT NULL, " +
            TASKS_COLUMN_DESC + " TEXT, " +
            TASKS_COLUMN_PRIORITY + " INTEGER, " +
            TASKS_COLUMN_CYCLE + " INTEGER DEFAULT 0, " +
            TASKS_COLUMN_DONE + " INTEGER DEFAULT 0, " +
            TASKS_COLUMN_DELETED + " INTEGER DEFAULT 0, " +
            COLUMN_ORDER + " INTEGER, " +
            TASKS_COLUMN_LIST + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + TASKS_COLUMN_LIST + ") REFERENCES " +
                TASKLIST_TABLE_NAME + "(" + COLUMN_ID + ")" +
            TASKS_COLUMN_DUEDATE + " DATE, " +
            TASKS_COLUMN_TODAYDATE + " DATE " +
         ");";

    static final String TASKS_VIEW_TODAY_NAME = "today";
    private static final String TASKS_VIEW_TODAY_CREATE =
            "CREATE VIEW IF NOT EXISTS " + TASKS_VIEW_TODAY_NAME + " AS" +
                    " SELECT * FROM " + TASKS_TABLE_NAME +
                    " WHERE " + TASKS_COLUMN_TODAYDATE + " = date('now')";
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TASKLIST_TABLE_CREATE);
        db.execSQL(TASKS_TABLE_CREATE);
        db.execSQL(TASKS_VIEW_TODAY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Fall-through is intended
        switch (oldVersion) {
            case 1:
                // Add new Order column
                db.execSQL("ALTER TABLE " + TASKLIST_TABLE_NAME + " ADD COLUMN " + COLUMN_ORDER + " INTEGER");
            case 2:
                // Add new Visible column
                db.execSQL("ALTER TABLE " + TASKLIST_TABLE_NAME + " ADD COLUMN " + TASKLIST_COLUMN_VISIBLE + " INTEGER DEFAULT 1");
                // Add new Due Date column
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_DUEDATE + " DATE");
            case 3:
                // Add new Today Date column
                db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME + " ADD COLUMN " + TASKS_COLUMN_TODAYDATE + " DATE");
                // Create the Today view
                db.execSQL(TASKS_VIEW_TODAY_CREATE);
        }
    }
}
