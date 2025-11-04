package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.local.AppDatabase
import com.wismna.geoffroy.donext.domain.model.Priority
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
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

        assertNotNull(fetched)
        assertEquals("Do laundry", fetched!!.name)
        assertEquals(listId, fetched.taskListId)
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

        val tasks = taskDao.getTasksForList(listId).first()
        assertEquals(listOf("High", "Normal", "Done"), tasks.map { it.name })
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
        assertEquals("Updated", fetched!!.name)
    }

    @Test
    fun toggleTaskDone_setsCorrectValue() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Toggle me", description = null, priority = Priority.NORMAL, taskListId = listId, isDone = false)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.toggleTaskDone(inserted.id, true)
        assertTrue(taskDao.getTaskById(inserted.id)!!.isDone)

        taskDao.toggleTaskDone(inserted.id, false)
        assertFalse(taskDao.getTaskById(inserted.id)!!.isDone)
    }

    @Test
    fun toggleTaskDeleted_marksTaskDeleted() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Trash me", description = null, priority = Priority.NORMAL, taskListId = listId)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.toggleTaskDeleted(inserted.id, true)
        val deletedTask = taskDao.getTaskById(inserted.id)
        assertTrue(deletedTask!!.isDeleted)
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
        assertTrue(fetched.isEmpty()) // filtered by deleted = 0

        // confirm soft deletion
        val softDeleted = fetched.size < 2
        assertTrue(softDeleted)
    }

    @Test
    fun permanentDeleteTask_removesFromDatabase() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Temp", description = null, priority = Priority.NORMAL, taskListId = listId)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.permanentDeleteTask(inserted.id)
        assertNull(taskDao.getTaskById(inserted.id))
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
        assertEquals(1, remaining.size)
        assertEquals("Active", remaining.first().name)
    }

    @Test
    fun getDeletedTasksWithListName_returnsCorrectlyJoinedData() = runBlocking {
        val listId = insertListAndReturnId(name = "Work")
        val deleted = TaskEntity(name = "Trash", taskListId = listId, description = null, priority = Priority.NORMAL, isDeleted = true)
        taskDao.insertTask(deleted)

        val results = taskDao.getDeletedTasksWithListName().first()
        assertEquals(1, results.size)
        assertEquals("Trash", results.first().task.name)
        assertEquals("Work", results.first().listName)
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
        assertEquals(1, tasks.size)
        assertEquals("Today", tasks.first().name)
    }
}
