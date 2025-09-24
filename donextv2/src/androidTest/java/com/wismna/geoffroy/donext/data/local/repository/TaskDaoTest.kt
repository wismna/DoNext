package com.wismna.geoffroy.donext.data.local.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.local.AppDatabase
import com.wismna.geoffroy.donext.data.local.dao.TaskDao
import com.wismna.geoffroy.donext.data.local.dao.TaskListDao
import com.wismna.geoffroy.donext.domain.model.Priority
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var listDao: TaskListDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        taskDao = db.taskDao()
        listDao = db.taskListDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun overdueCount_correctlyCalculated() = runBlocking {
        listDao.insertTaskList(TaskListEntity(name = "Work", order = 0))
        val listId = listDao.getTaskLists().first().first().id

        val now = Instant.parse("2025-09-15T12:00:00Z").toEpochMilli()

        // One overdue task (yesterday)
        taskDao.insertTask(
            TaskEntity(
                name = "Finish report",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-14T12:00:00Z").toEpochMilli(),
                isDone = false,
                description = null,
                priority = Priority.NORMAL
            )
        )

        // One not overdue task (tomorrow)
        taskDao.insertTask(
            TaskEntity(
                name = "Prepare slides",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-16T12:00:00Z").toEpochMilli(),
                isDone = false,
                description = null,
                priority = Priority.NORMAL
            )
        )

        // One done task (yesterday, but marked done)
        taskDao.insertTask(
            TaskEntity(
                name = "Old task",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-14T12:00:00Z").toEpochMilli(),
                isDone = true,
                description = null,
                priority = Priority.NORMAL
            )
        )

        val lists = listDao.getTaskListsWithOverdue(now)

        TestCase.assertEquals(1, lists.first().first().overdueCount)
    }

    @Test
    fun dueToday_correctlyCalculated() = runBlocking {
        listDao.insertTaskList(TaskListEntity(name = "Tasks", order = 0))
        val listId = listDao.getTaskLists().first().first().id

        val todayStart = Instant.parse("2025-09-15T00:00:00Z").toEpochMilli()
        val todayEnd = Instant.parse("2025-09-15T23:59:99Z").toEpochMilli()

        // One task due yesterday
        taskDao.insertTask(
            TaskEntity(
                name = "Yesterday",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-14T12:00:00Z").toEpochMilli(),
                isDone = false,
                description = null,
                priority = Priority.NORMAL
            )
        )
        // One task due today
        taskDao.insertTask(
            TaskEntity(
                name = "Today",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-15T12:00:00Z").toEpochMilli(),
                isDone = false,
                description = null,
                priority = Priority.NORMAL
            )
        )
        // One task due in the future
        taskDao.insertTask(
            TaskEntity(
                name = "Tomorrow",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-16T12:00:00Z").toEpochMilli(),
                isDone = false,
                description = null,
                priority = Priority.NORMAL
            )
        )
        // One task due in the future
        taskDao.insertTask(
            TaskEntity(
                name = "TodayDone",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-15T12:00:00Z").toEpochMilli(),
                isDone = true,
                description = null,
                priority = Priority.NORMAL
            )
        )
        // One task due in the future
        taskDao.insertTask(
            TaskEntity(
                name = "TodayDeleted",
                taskListId = listId,
                dueDate = Instant.parse("2025-09-15T12:00:00Z").toEpochMilli(),
                isDone = false,
                isDeleted = true,
                description = null,
                priority = Priority.NORMAL
            )
        )

        val tasks = taskDao.getDueTodayTasks(todayStart, todayEnd)

        TestCase.assertEquals(1, tasks.first().count())
        TestCase.assertEquals("Prepare slides", tasks.first().first().name)
    }
}