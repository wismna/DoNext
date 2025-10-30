package com.wismna.geoffroy.donext.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.data.Converters
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.local.dao.TaskDao
import com.wismna.geoffroy.donext.data.local.dao.TaskListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TaskEntity::class, TaskListEntity::class],
    version = 7
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao

    companion object {
        @Volatile
        private var DB_INSTANCE: AppDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    // --- TASKS TABLE ---

                    // 0. Convert old due date format
                    // Add temporary column
                    db.execSQL("ALTER TABLE tasks ADD COLUMN duedate_temp INTEGER")
                    // Populate temporary column
                    db.execSQL("""
                        WITH offset AS (
                            SELECT (strftime('%s', 'now', 'localtime') - strftime('%s', 'now')) / 3600.0 AS diff
                        )
                        UPDATE tasks
                        SET duedate_temp = 
                            CASE
                                WHEN duedate IS NULL OR duedate = '' THEN NULL
                                ELSE (
                                    strftime(
                                        '%s',
                                        duedate || ' 00:00:00',
                                        CASE
                                            WHEN (SELECT diff FROM offset) >= 0
                                                THEN '-' || (SELECT diff FROM offset) || ' hours'
                                            ELSE '+' || abs((SELECT diff FROM offset)) || ' hours'
                                        END
                                    ) * 1000
                                )
                            END
                    """.trimIndent())

                    // 1. Create the new tasks table with the updated schema
                    db.execSQL(
                        """
                        CREATE TABLE tasks_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT,
                            priority INTEGER NOT NULL,
                            done INTEGER NOT NULL DEFAULT 0,
                            deleted INTEGER NOT NULL DEFAULT 0,
                            task_list_id INTEGER NOT NULL,
                            due_date INTEGER
                        )
                        """.trimIndent()
                    )

                    // 2. Copy old data into the new table
                    // Map old column names to new ones
                    db.execSQL(
                        """
                        INSERT INTO tasks_new (id, name, description, priority,done, deleted, task_list_id, due_date)
                        SELECT _id, name, description, priority, done, deleted, list, duedate_temp
                        FROM tasks
                        """.trimIndent()
                    )

                    // 3. Drop the old table
                    db.execSQL("DROP TABLE tasks")

                    // 4. Rename the new table
                    db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

                    // --- TASK_LISTS TABLE ---
                    db.execSQL(
                        """
                        CREATE TABLE task_lists_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            deleted INTEGER NOT NULL DEFAULT 0,
                            display_order INTEGER NOT NULL
                        )
                    """.trimIndent()
                    )

                    db.execSQL(
                        """
                        INSERT INTO task_lists_new (id, name, display_order, deleted)
                        SELECT _id, name, displayorder, 1 - visible
                        FROM tasklist
                    """.trimIndent()
                    )

                    db.execSQL("DROP TABLE tasklist")
                    db.execSQL("ALTER TABLE task_lists_new RENAME TO task_lists")

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

        fun buildDatabase(context: Context): AppDatabase {
            return DB_INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "donext.db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration(false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // insert default lists
                            CoroutineScope(Dispatchers.IO).launch {
                                val res = context.resources
                                val dao = DB_INSTANCE?.taskListDao()
                                dao?.insertTaskList(TaskListEntity(name = res.getString(R.string.sample_list_personal), order = 1))
                                dao?.insertTaskList(TaskListEntity(name = res.getString(R.string.sample_list_work), order = 2))
                                dao?.insertTaskList(TaskListEntity(name = res.getString(R.string.sample_list_shopping), order = 3))
                            }
                        }
                    })
                    .build()
                DB_INSTANCE = instance
                return instance
            }
        }
    }
}