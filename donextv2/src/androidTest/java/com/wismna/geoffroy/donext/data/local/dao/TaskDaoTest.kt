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
import java.time.LocalDate
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
    fun updateTaskDetails_updatesEditableFieldsOnly() = runBlocking {
        val listId = insertListAndReturnId()
        val id = taskDao.insertTask(
            TaskEntity(
                name = "Old", description = null, priority = Priority.NORMAL, taskListId = listId,
                isDone = true, createdAt = 42L, completedAt = 43L, isReminderShown = true
            )
        )

        taskDao.updateTaskDetails(id, listId, "Updated", "Desc", Priority.HIGH, LocalDate.of(2026, 3, 10), 600)

        val fetched = taskDao.getTaskById(id)!!
        assertThat(fetched.name).isEqualTo("Updated")
        assertThat(fetched.description).isEqualTo("Desc")
        assertThat(fetched.priority).isEqualTo(Priority.HIGH)
        assertThat(fetched.dueDate).isEqualTo(LocalDate.of(2026, 3, 10))
        assertThat(fetched.dueTimeMinutes).isEqualTo(600)
        assertThat(fetched.isDone).isTrue()
        assertThat(fetched.createdAt).isEqualTo(42L)
        assertThat(fetched.completedAt).isEqualTo(43L)
        assertThat(fetched.isReminderShown).isTrue()
    }

    @Test
    fun toggleTaskDone_setsCorrectValue() = runBlocking {
        val listId = insertListAndReturnId()
        val task = TaskEntity(name = "Toggle me", description = null, priority = Priority.NORMAL, taskListId = listId, isDone = false)
        taskDao.insertTask(task)
        val inserted = taskDao.getTasksForList(listId).first().first()

        taskDao.toggleTaskDone(inserted.id, true, 500L)
        assertThat(taskDao.getTaskById(inserted.id)!!.isDone).isTrue()
        assertThat(taskDao.getTaskById(inserted.id)!!.completedAt).isEqualTo(500L)

        taskDao.toggleTaskDone(inserted.id, false, null)
        assertThat(taskDao.getTaskById(inserted.id)!!.isDone).isFalse()
        assertThat(taskDao.getTaskById(inserted.id)!!.completedAt).isNull()
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

        val yesterday = TaskEntity(
            name = "Yesterday",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = LocalDate.of(2025, 9, 14)
        )
        val today = TaskEntity(
            name = "Today",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = LocalDate.of(2025, 9, 15)
        )
        val tomorrow = TaskEntity(
            name = "Tomorrow",
            description = null,
            priority = Priority.NORMAL,
            taskListId = listId,
            dueDate = LocalDate.of(2025, 9, 16)
        )

        taskDao.insertTask(yesterday)
        taskDao.insertTask(today)
        taskDao.insertTask(tomorrow)

        val tasks = taskDao.getDueTodayTasks(LocalDate.of(2025, 9, 15)).first()
        assertThat(tasks).hasSize(1)
        assertThat(tasks.first().name).isEqualTo("Today")
    }

    @Test
    fun insertTask_returnsGeneratedIdAndPersistsNewColumns() = runBlocking {
        val listId = insertListAndReturnId()
        val id = taskDao.insertTask(
            TaskEntity(name = "Call", description = null, priority = Priority.NORMAL, taskListId = listId,
                dueDate = LocalDate.of(2026, 3, 10), dueTimeMinutes = 870, createdAt = 42L)
        )

        val fetched = taskDao.getTaskById(id)!!
        assertThat(fetched.dueTimeMinutes).isEqualTo(870)
        assertThat(fetched.createdAt).isEqualTo(42L)
    }

    @Test
    fun getOpenTasksDueBy_returnsOnlyOpenTasksDueUpToTheLimit() = runBlocking<Unit> {
        val listId = insertListAndReturnId()
        fun task(name: String, dueDate: LocalDate?, isDone: Boolean = false, isDeleted: Boolean = false) = TaskEntity(
            name = name, description = null, priority = Priority.NORMAL, taskListId = listId,
            dueDate = dueDate, isDone = isDone, isDeleted = isDeleted
        )
        taskDao.insertTask(task("Before", LocalDate.of(2026, 3, 9)))
        taskDao.insertTask(task("AtLimit", LocalDate.of(2026, 3, 10)))
        taskDao.insertTask(task("After", LocalDate.of(2026, 3, 11)))
        taskDao.insertTask(task("Undated", null))
        taskDao.insertTask(task("Done", LocalDate.of(2026, 3, 9), isDone = true))
        taskDao.insertTask(task("Deleted", LocalDate.of(2026, 3, 9), isDeleted = true))

        val names = taskDao.getOpenTasksDueBy(LocalDate.of(2026, 3, 10)).map { it.name }
        assertThat(names).containsExactly("Before", "AtLimit")
    }

    @Test
    fun getOpenTasksWithDueTime_skipsUntimedDoneAndDeletedTasks() = runBlocking<Unit> {
        val listId = insertListAndReturnId()
        fun task(name: String, time: Int?, isDone: Boolean = false, isDeleted: Boolean = false) = TaskEntity(
            name = name, description = null, priority = Priority.NORMAL, taskListId = listId,
            dueDate = LocalDate.of(2026, 3, 10), dueTimeMinutes = time, isDone = isDone, isDeleted = isDeleted
        )
        taskDao.insertTask(task("Timed", 600))
        taskDao.insertTask(task("Untimed", null))
        taskDao.insertTask(task("Done", 600, isDone = true))
        taskDao.insertTask(task("Deleted", 600, isDeleted = true))

        val names = taskDao.getOpenTasksWithDueTime().map { it.name }
        assertThat(names).containsExactly("Timed")
    }

    @Test
    fun getTaskIdsForList_includesDeletedTasksOfThatListOnly() = runBlocking<Unit> {
        val listId = insertListAndReturnId(name = "A", order = 0)
        listDao.insertTaskList(TaskListEntity(name = "B", order = 1))
        val otherListId = listDao.getTaskLists().first().first { it.name == "B" }.id
        val id1 = taskDao.insertTask(TaskEntity(name = "1", description = null, priority = Priority.NORMAL, taskListId = listId))
        val id2 = taskDao.insertTask(TaskEntity(name = "2", description = null, priority = Priority.NORMAL, taskListId = listId, isDeleted = true))
        taskDao.insertTask(TaskEntity(name = "3", description = null, priority = Priority.NORMAL, taskListId = otherListId))

        assertThat(taskDao.getTaskIdsForList(listId)).containsExactly(id1, id2)
    }

    @Test
    fun setReminderShown_setsAndClearsTheFlag() = runBlocking {
        val listId = insertListAndReturnId()
        val id = taskDao.insertTask(TaskEntity(name = "Call", description = null, priority = Priority.NORMAL, taskListId = listId))
        assertThat(taskDao.getTaskById(id)!!.isReminderShown).isFalse()

        taskDao.setReminderShown(id, true)
        assertThat(taskDao.getTaskById(id)!!.isReminderShown).isTrue()

        taskDao.setReminderShown(id, false)
        assertThat(taskDao.getTaskById(id)!!.isReminderShown).isFalse()
    }
}
