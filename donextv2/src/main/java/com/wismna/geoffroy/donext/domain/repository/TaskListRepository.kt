package com.wismna.geoffroy.donext.domain.repository

import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import kotlinx.coroutines.flow.Flow

interface TaskListRepository {
    fun getTaskLists(): Flow<List<TaskList>>
    suspend fun getTaskListById(taskListId: Long): TaskList?
    suspend fun insertTaskList(taskList: TaskList)
    suspend fun updateTaskList(taskList: TaskList)
    suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean)
    fun getTaskListsWithOverdue(nowMillis: Long): Flow<List<TaskListWithOverdue>>
}
