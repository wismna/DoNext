package com.wismna.geoffroy.donext.data.local

import android.content.ContentValues
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * This test recreates the old SQLite schema (v6 from DatabaseHelper),
     * inserts sample legacy rows, runs AppDatabase.MIGRATION_6_7 and then
     * validates the migrated data by calling the real DAOs you provided.
     */
    @Test
    @Throws(IOException::class)
    fun migrate_v6_to_v7_preserves_data_and_transforms_columns() {
        // Arrange
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val db: SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 6)

        val listValues = ContentValues().apply {
            put("_id", 1)
            put("name", "Legacy List")
            put("displayorder", 10)
            put("visible", 1)
            put("taskCount", 0)
        }
        db.insert("tasklist", 0, listValues)

        val taskValues = ContentValues().apply {
            put("_id", 1) // explicit id
            put("name", "Legacy Task")
            put("description", "Old task description")
            put("priority", 2)
            put("cycle", 0)
            put("done", 0)
            put("deleted", 0)
            put("displayorder", 5)
            put("todayorder", 0)
            put("list", 1) // references tasklist _id = 1
            put("duedate", "2025-09-15") // legacy text date format that migration converts
            put("todaydate", null as String?)
        }
        db.insert("tasks", 0, taskValues)
        db.close()

        // Act
        helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)

        val migratedRoom = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8)
            .build()

        // Assert
        try {
            val listDao = migratedRoom.taskListDao()
            val taskDao = migratedRoom.taskDao()

            runBlocking {
                val migratedList = listDao.getTaskListById(1L)
                assertThat(migratedList).isNotNull()
                assertThat(migratedList!!.id).isEqualTo(1L)
                assertThat(migratedList.name).isEqualTo("Legacy List")
                assertThat(migratedList.isDeleted).isEqualTo(false)
            }

            runBlocking {
                val migratedTask = taskDao.getTaskById(1L)
                assertThat(migratedTask).isNotNull()
                assertThat(migratedTask!!.id).isEqualTo(1L)
                assertThat(migratedTask.name).isEqualTo("Legacy Task")
                assertThat(migratedTask.description).isEqualTo("Old task description")
                assertThat(migratedTask.priority).isEqualTo(Priority.HIGH)
                assertThat(migratedTask.isDone).isEqualTo(false)
                assertThat(migratedTask.isDeleted).isEqualTo(false)
                assertThat(migratedTask.dueDate).isEqualTo(LocalDate.of(2025, 9, 15))
                assertThat(migratedTask.taskListId).isEqualTo(1L)
            }
        } finally {
            migratedRoom.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate_v7_to_v8_adds_reminder_and_timestamp_columns() {
        val dueDate = LocalDate.of(2026, 3, 10)
        val localMidnightMillis = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val db = helper.createDatabase(TEST_DB, 7)
        db.execSQL("INSERT INTO task_lists (id, name, deleted, display_order) VALUES (1, 'List', 0, 1)")
        db.execSQL(
            "INSERT INTO tasks (id, name, description, priority, done, deleted, task_list_id, due_date) " +
                "VALUES (1, 'Task', NULL, 1, 1, 0, 1, $localMidnightMillis)"
        )
        db.close()

        val beforeMigration = System.currentTimeMillis() - 1_000
        val migrated = helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_7_8)

        migrated.query("SELECT due_date, due_time_minutes, created_at, completed_at, done, reminder_shown FROM tasks WHERE id = 1").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getLong(0)).isEqualTo(dueDate.toEpochDay())
            assertThat(cursor.isNull(1)).isTrue()
            assertThat(cursor.getLong(2)).isAtLeast(beforeMigration)
            assertThat(cursor.isNull(3)).isTrue()
            assertThat(cursor.getInt(4)).isEqualTo(1)
            assertThat(cursor.getInt(5)).isEqualTo(0)
        }
        migrated.close()
    }
}
