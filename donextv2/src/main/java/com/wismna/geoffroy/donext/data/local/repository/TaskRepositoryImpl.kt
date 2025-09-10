package com.wismna.geoffroy.donext.data.local.repository

import com.wismna.geoffroy.donext.data.local.dao.TaskDao
import com.wismna.geoffroy.donext.data.local.dao.TaskListDao
import com.wismna.geoffroy.donext.data.toDomain
import com.wismna.geoffroy.donext.data.toEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
): TaskRepository {
    override suspend fun getTasksForList(listId: Long): List<Task> {
        return taskDao.getTasksForList(listId).map { it.toDomain() }
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        val updated = task.copy(updateDate = Instant.now())
        taskDao.updateTask(updated.toEntity())
    }

    override suspend fun deleteTask(taskId: Long, isDeleted: Boolean) {
        taskDao.markTaskDeleted(taskId, isDeleted)
    }

    override suspend fun closeTask(taskId: Long, isDone: Boolean) {
        taskDao.markTaskDone(taskId, isDone)
    }

    override suspend fun increaseTaskCycle(taskId: Long) {
        taskDao.increaseCycle(taskId)
    }

    override fun getTaskLists(): Flow<List<TaskList>> {
        return taskListDao.getTaskLists().map {entities -> entities.map { it.toDomain() }}
    }

    override suspend fun insertTaskList(taskList: TaskList) {
        taskListDao.insertTaskList(taskList.toEntity())
    }

    override suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean) {
        taskDao.deleteAllTasksFromList(taskListId, isDeleted)
        taskListDao.deleteTaskList(taskListId, isDeleted)
    }
}