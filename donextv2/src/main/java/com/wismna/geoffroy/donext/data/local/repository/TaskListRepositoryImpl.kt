package com.wismna.geoffroy.donext.data.local.repository

import com.wismna.geoffroy.donext.data.local.dao.TaskListDao
import com.wismna.geoffroy.donext.data.toDomain
import com.wismna.geoffroy.donext.data.toEntity
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskListRepositoryImpl @Inject constructor(
    private val taskListDao: TaskListDao
): TaskListRepository {
    override fun getTaskLists(): Flow<List<TaskList>> {
        return taskListDao.getTaskLists().map {entities -> entities.map { it.toDomain() }}
    }

    override suspend fun getTaskListById(taskListId: Long): TaskList? {
        return taskListDao.getTaskListById(taskListId)?.toDomain()
    }

    override suspend fun insertTaskList(taskList: TaskList) {
        taskListDao.insertTaskList(taskList.toEntity())
    }

    override suspend fun updateTaskList(taskList: TaskList) {
        taskListDao.updateTaskList(taskList.toEntity())
    }

    override suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean) {
        taskListDao.deleteTaskList(taskListId, isDeleted)
    }

    override fun getTaskListsWithOverdue(nowMillis: Long): Flow<List<TaskListWithOverdue>> {
        return taskListDao.getTaskListsWithOverdue(nowMillis)
    }
}
