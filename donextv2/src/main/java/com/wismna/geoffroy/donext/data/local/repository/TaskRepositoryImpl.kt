package com.wismna.geoffroy.donext.data.local.repository

import com.wismna.geoffroy.donext.data.local.dao.TaskDao
import com.wismna.geoffroy.donext.data.local.dao.TaskListDao
import com.wismna.geoffroy.donext.data.toDomain
import com.wismna.geoffroy.donext.data.toEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
): TaskRepository {
    override fun getTasksForList(listId: Long): Flow<List<Task>> {
        return taskDao.getTasksForList(listId).map {entity -> entity.map { it.toDomain() }}
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(taskId: Long, isDeleted: Boolean) {
        taskDao.toggleTaskDeleted(taskId, isDeleted)
    }

    override suspend fun toggleTaskDone(taskId: Long, isDone: Boolean) {
        taskDao.toggleTaskDone(taskId, isDone)
    }

    override fun getTaskLists(): Flow<List<TaskList>> {
        return taskListDao.getTaskLists().map {entities -> entities.map { it.toDomain() }}
    }

    override suspend fun insertTaskList(taskList: TaskList) {
        taskListDao.insertTaskList(taskList.toEntity())
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        taskListDao.updateTaskList(taskList.toEntity())
    }

    override suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean) {
        taskDao.toggleAllTasksFromListDeleted(taskListId, isDeleted)
        taskListDao.deleteTaskList(taskListId, isDeleted)
    }

    override fun getTaskListsWithOverdue(nowMillis: Long): Flow<List<TaskListWithOverdue>> {
        return taskListDao.getTaskListsWithOverdue(nowMillis).map { it }
    }
}