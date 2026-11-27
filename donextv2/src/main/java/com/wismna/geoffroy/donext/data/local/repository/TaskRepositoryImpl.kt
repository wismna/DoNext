package com.wismna.geoffroy.donext.data.local.repository

import com.wismna.geoffroy.donext.data.local.dao.TaskDao
import com.wismna.geoffroy.donext.data.toDomain
import com.wismna.geoffroy.donext.data.toEntity
import com.wismna.geoffroy.donext.data.toMinutesOfDay
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
): TaskRepository {
    override fun getTasksForList(listId: Long): Flow<List<Task>> {
        return taskDao.getTasksForList(listId).map {entity -> entity.map { it.toDomain() }}
    }

    override fun getDueTodayTasks(today: LocalDate): Flow<List<Task>> {
        return taskDao.getDueTodayTasks(today).map {entity -> entity.map { it.toDomain() }}
    }

    override fun getDeletedTasks(): Flow<List<TaskWithListName>> {
        return taskDao.getDeletedTasksWithListName().map {entity -> entity.map { it.toDomain() }}
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun getOpenTasksDueBy(date: LocalDate): List<Task> {
        return taskDao.getOpenTasksDueBy(date).map { it.toDomain() }
    }

    override suspend fun getOpenTasksWithDueTime(): List<Task> {
        return taskDao.getOpenTasksWithDueTime().map { it.toDomain() }
    }

    override suspend fun getTaskIdsForList(taskListId: Long): List<Long> {
        return taskDao.getTaskIdsForList(taskListId)
    }

    override suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTaskDetails(task: Task) {
        val taskId = requireNotNull(task.id) { "Only a saved task can be updated" }
        taskDao.updateTaskDetails(
            taskId = taskId,
            taskListId = task.taskListId,
            name = task.name,
            description = task.description,
            priority = task.priority,
            dueDate = task.dueDate,
            dueTimeMinutes = task.dueTime?.toMinutesOfDay()
        )
    }

    override suspend fun toggleTaskDeleted(taskId: Long, isDeleted: Boolean) {
        taskDao.toggleTaskDeleted(taskId, isDeleted)
    }

    override suspend fun toggleTaskDone(taskId: Long, isDone: Boolean, completedAt: Long?) {
        taskDao.toggleTaskDone(taskId, isDone, completedAt)
    }

    override suspend fun setReminderShown(taskId: Long, isShown: Boolean) {
        taskDao.setReminderShown(taskId, isShown)
    }

    override suspend fun permanentlyDeleteTask(taskId: Long) {
        taskDao.permanentDeleteTask(taskId)
    }

    override suspend fun permanentlyDeleteAllDeletedTask() {
        taskDao.permanentDeleteAllDeletedTasks()
    }

    override suspend fun toggleAllTasksInListDeleted(taskListId: Long, isDeleted: Boolean) {
        taskDao.toggleAllTasksFromListDeleted(taskListId, isDeleted)
    }
}
