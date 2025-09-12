package com.wismna.geoffroy.donext.domain.repository

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForList(listId: Long): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long, isDeleted: Boolean)
    suspend fun closeTask(taskId: Long, isDone: Boolean)
    suspend fun increaseTaskCycle(taskId: Long)

    fun getTaskLists(): Flow<List<TaskList>>
    suspend fun insertTaskList(taskList: TaskList)
    suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean)
}