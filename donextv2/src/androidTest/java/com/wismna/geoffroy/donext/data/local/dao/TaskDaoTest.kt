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

    private suspend fun insertListAndReturnId(name: String = "List", order: Int = 0): Long {
        listDao.insertTaskList(TaskListEntity(name = name, order = order))
        return listDao.getTaskLists().first().first().id
    }

    @Test
    fun insertAndGetTaskById_worksCorrectly() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(
            name = "Do laundry",
            description = null,
            taskListId = listId,
            priority = Priority.NORMAL
        )

        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()
        val fetched = taskDao.getTaskById(inserted.id)

        assertThat(fetched).isNotNull()
        assertThat(fetched!!.name).isEqualTo("Do laundry")
        assertThat(fetched.taskListId).isEqualTo(listId)
    }

    @Test
    fun getTasksForList_returnsOrderedByDoneAndPriority() = runBlocking {
        val listId = insertListAndReturnId()
        val high = TaskEntity(name = "High", description = null, priority = Priority.HIGH, taskListId = listId)
        val normal = TaskEntity(name = "Normal", description = null, priority = Priority.NORMAL, taskListId = listId)
        val done = TaskEntity(name = "Done", description = null, priority = Priority.NORMAL, taskListId = listId, isDone = true)

        taskDao.insertTask(normal)
        taskDao.insertTask(done)
        taskDao.insertTask(high)

        val taskPriorities = taskDao.getTasksForList(listId).first().map { it.name }
        assertThat(taskPriorities).containsExactly("High", "Normal", "Done").inOrder()
    }

    @Test
    fun updateTask_updatesFields() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Old", description = null, priority = Priority.NORMAL, taskListId = listId)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        val updated = inserted.copy(name = "Updated")
        taskDao.updateTask(updated)

        val fetched = taskDao.getTaskById(inserted.id)
        assertThat(fetched!!.name).isEqualTo("Updated")
    }

    @Test
    fun toggleTaskDone_setsCorrectValue() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Toggle me", description = null, priority = Priority.NORMAL, taskListId = listId, isDone = false)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.toggleTaskDone(inserted.id, true)
        assertThat(taskDao.getTaskById(inserted.id)!!.isDone).isTrue()

        taskDao.toggleTaskDone(inserted.id, false)
        assertThat(taskDao.getTaskById(inserted.id)!!.isDone).isFalse()
    }

    @Test
    fun toggleTaskDeleted_marksTaskDeleted() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Trash me", description = null, priority = Priority.NORMAL, taskListId = listId)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.toggleTaskDeleted(inserted.id, true)
        val deletedTask = taskDao.getTaskById(inserted.id)
        assertThat(deletedTask!!.isDeleted).isTrue()
    }

    @Test
    fun toggleAllTasksFromListDeleted_marksAllDeleted() = runBlocking {
        val listId = insertListAndReturnId()
        val tasks = listOf(
            TaskEntity(name = "A", description = null, priority = Priority.NORMAL, taskListId = listId),
            TaskEntity(name = "B", description = null, priority = Priority.NORMAL, taskListId = listId)
        )
        tasks.forEach { taskDao.insertTask(it) }

        taskDao.toggleAllTasksFromListDeleted(listId, true)
        val fetched = taskDao.getTasksForList(listId).first()
        assertThat(fetched).isEmpty()

        // confirm soft deletion
        assertThat(fetched).hasSize(0)
    }

    @Test
    fun permanentDeleteTask_removesFromDatabase() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Temp", description = null, priority = Priority.NORMAL, taskListId = listId)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.permanentDeleteTask(inserted.id)
        assertThat(taskDao.getTaskById(inserted.id)).isNull()
    }

    @Test
    fun permanentDeleteAllDeletedTasks_removesAllDeleted() = runBlocking {
        val listId = insertListAndReturnId()
        val active = TaskEntity(name = "Active", description = null, priority = Priority.NORMAL, taskListId = listId)
        val deleted = TaskEntity(name = "Deleted", description = null, priority = Priority.NORMAL, taskListId = listId, isDeleted = true)
        taskDao.insertTask(active)
        taskDao.insertTask(deleted)

        taskDao.permanentDeleteAllDeletedTasks()
        val remaining = taskDao.getTasksForList(listId).first()
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().name).isEqualTo("Active")
    }

    @Test
    fun getDeletedTasksWithListName_returnsCorrectlyJoinedData() = runBlocking {
        val listId = insertListAndReturnId(name = "Work")
        val deleted = TaskEntity(name = "Trash", taskListId = listId, description = null, priority = Priority.NORMAL, isDeleted = true)
        taskDao.insertTask(deleted)

        val results = taskDao.getDeletedTasksWithListName().first()
        assertThat(results).hasSize(1)
        assertThat(results.first().task.name).isEqualTo("Trash")
        assertThat(results.first().listName).isEqualTo("Work")
    }

    @Test
    fun getDueTodayTasks_returnsTasksWithinRange() = runBlocking {
        val listId = insertListAndReturnId()
        val todayStart = Instant.parse("2025-09-15T00:00:00Z").toEpochMilli()
        val todayEnd = Instant.parse("2025-09-15T23:59:59Z").toEpochMilli()

        val yesterday = TaskEntity(
            name = "Yesterday",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = Instant.parse("2025-09-14T12:00:00Z").toEpochMilli()
        )
        val today = TaskEntity(
            name = "Today",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = Instant.parse("2025-09-15T12:00:00Z").toEpochMilli()
        )
        val tomorrow = TaskEntity(
            name = "Tomorrow",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = Instant.parse("2025-09-16T12:00:00Z").toEpochMilli()
        )

        taskDao.insertTask(yesterday)
        taskDao.insertTask(today)
        taskDao.insertTask(tomorrow)

        val tasks = taskDao.getDueTodayTasks(todayStart, todayEnd).first()
        assertThat(tasks).hasSize(1)
        assertThat(tasks.first().name).isEqualTo("Today")
    }
}
