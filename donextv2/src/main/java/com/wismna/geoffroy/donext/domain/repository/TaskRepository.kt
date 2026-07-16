package com.wismna.geoffroy.donext.domain.repository

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForList(listId: Long): Flow<List<Task>>
    fun getDueTodayTasks(todayStart: Long, todayEnd: Long): Flow<List<Task>>
    fun getDeletedTasks(): Flow<List<TaskWithListName>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun toggleTaskDeleted(taskId: Long, isDeleted: Boolean)
    suspend fun toggleTaskDone(taskId: Long, isDone: Boolean)
    suspend fun permanentlyDeleteTask(taskId: Long)
    suspend fun permanentlyDeleteAllDeletedTask()
    suspend fun toggleAllTasksInListDeleted(taskListId: Long, isDeleted: Boolean)
}
