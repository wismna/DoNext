package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.local.AppDatabase
import com.wismna.geoffroy.donext.domain.model.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.collections.first

@RunWith(AndroidJUnit4::class)
class TaskListDaoTest {

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
    fun insertTaskList_insertsSuccessfully() = runBlocking {
        val taskList = TaskListEntity(name = "Personal", order = 1)
        listDao.insertTaskList(taskList)

        val lists = listDao.getTaskLists().first()
        assertThat(lists).hasSize(1)
        assertThat(lists.first().name).isEqualTo("Personal")
    }

    @Test
    fun getTaskListById_returnsCorrectEntity() = runBlocking {
        val taskList = TaskListEntity(name = "Groceries", order = 0)
        listDao.insertTaskList(taskList)

        val inserted = listDao.getTaskLists().first().first()
        val fetched = listDao.getTaskListById(inserted.id)

        assertThat(fetched).isNotNull()
        assertThat(fetched!!.name).isEqualTo("Groceries")
        assertThat(fetched.id).isEqualTo(inserted.id)
    }

    @Test
    fun updateTaskList_updatesSuccessfully() = runBlocking {
        val taskList = TaskListEntity(name = "Work", order = 0)
        listDao.insertTaskList(taskList)

        val inserted = listDao.getTaskLists().first().first()
        val updated = inserted.copy(name = "Updated Work")
        listDao.updateTaskList(updated)

        val fetched = listDao.getTaskListById(inserted.id)
        assertThat(fetched!!.name).isEqualTo("Updated Work")
    }

    @Test
    fun deleteTaskList_marksAsDeleted() = runBlocking {
        val taskList = TaskListEntity(name = "Errands", order = 0)
        listDao.insertTaskList(taskList)

        val inserted = listDao.getTaskLists().first().first()
        listDao.deleteTaskList(inserted.id, true)

        // getTaskLists() filters deleted = 0, so result should be empty
        val activeLists = listDao.getTaskLists().first()
        assertThat(activeLists).isEmpty()

        // But the entity still exists in DB
        val softDeleted = listDao.getTaskListById(inserted.id)
        assertThat(softDeleted).isNotNull()
        assertThat(softDeleted!!.isDeleted).isTrue()
    }

    @Test
    fun getTaskLists_returnsOrderedByDisplayOrder() = runBlocking {
        val first = TaskListEntity(name = "Zeta", order = 2)
        val second = TaskListEntity(name = "Alpha", order = 0)
        val third = TaskListEntity(name = "Beta", order = 1)
        listDao.insertTaskList(first)
        listDao.insertTaskList(second)
        listDao.insertTaskList(third)

        val listNames = listDao.getTaskLists().first().map { it.name }
        assertThat(listNames).containsExactly("Alpha", "Beta", "Zeta").inOrder()
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

        assertThat(lists.first().first().overdueCount).isEqualTo(1)
    }

    @Test
    fun dueToday_correctlyCalculated() = runBlocking {
        listDao.insertTaskList(TaskListEntity(name = "Tasks", order = 0))
        val listId = listDao.getTaskLists().first().first().id

        val todayStart = Instant.parse("2025-09-15T00:00:00Z").toEpochMilli()
        val todayEnd = Instant.parse("2025-09-15T23:59:59Z").toEpochMilli()

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
        // One task due today but done
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
        // One task due today but deleted
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

        assertThat(tasks.first()).hasSize(1)
        assertThat(tasks.first().first().name).isEqualTo("Today")
    }
}