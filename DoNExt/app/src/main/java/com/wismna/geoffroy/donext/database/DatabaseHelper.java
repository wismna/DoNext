package com.wismna.geoffroy.donext.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by geoffroy on 15-11-25.
 * Database helper class that contains table and column names as well as handles database creation
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "donext.db";
    public static final String COLUMN_ID = "_id";

    public static final String TASKLIST_TABLE_NAME = "tasklist";
    public static final String TASKLIST_COLUMN_NAME = "name";
    public static final String TASKLIST_COLUMN_TASK_COUNT = "taskcount";
    private static final String TASKLIST_TABLE_CREATE =
            "CREATE TABLE " + TASKLIST_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASKLIST_COLUMN_NAME + " TEXT NOT NULL);";

    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_COLUMN_NAME = "name";
    public static final String TASKS_COLUMN_DESC = "description";
    public static final String TASKS_COLUMN_CYCLE = "cycle";
    public static final String TASKS_COLUMN_PRIORITY = "priority";
    public static final String TASKS_COLUMN_DONE = "done";
    public static final String TASKS_COLUMN_DELETED= "deleted";
    public static final String TASKS_COLUMN_LIST = "list";
    private static final String TASKS_TABLE_CREATE =
            "CREATE TABLE " + TASKS_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASKS_COLUMN_NAME + " TEXT NOT NULL, " +
                    TASKS_COLUMN_DESC + " TEXT, " +
                    TASKS_COLUMN_PRIORITY + " INTEGER, " +
                    TASKS_COLUMN_CYCLE + " INTEGER DEFAULT 0, " +
                    TASKS_COLUMN_DONE + " INTEGER DEFAULT 0, " +
                    TASKS_COLUMN_DELETED + " INTEGER DEFAULT 0, " +
                    TASKS_COLUMN_LIST + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + TASKS_COLUMN_LIST + ") REFERENCES " +
                    TASKLIST_TABLE_NAME + "(" + COLUMN_ID + ")" +
             ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TASKLIST_TABLE_CREATE);
        db.execSQL(TASKS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
