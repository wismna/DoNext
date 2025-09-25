package com.wismna.geoffroy.donext.domain.repository

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForList(listId: Long): Flow<List<Task>>
    fun getDueTodayTasks(todayStart: Long, todayEnd: Long): Flow<List<Task>>
    fun getDeletedTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun toggleTaskDeleted(taskId: Long, isDeleted: Boolean)
    suspend fun toggleTaskDone(taskId: Long, isDone: Boolean)
    suspend fun permanentlyDeleteTask(taskId: Long)
    suspend fun permanentlyDeleteAllDeletedTask()

    fun getTaskLists(): Flow<List<TaskList>>
    suspend fun insertTaskList(taskList: TaskList)
    suspend fun updateTaskList(taskList: TaskList)
    suspend fun deleteTaskList(taskListId: Long, isDeleted: Boolean)
    fun getTaskListsWithOverdue(nowMillis: Long): Flow<List<TaskListWithOverdue>>
}