package com.wismna.geoffroy.donext.domain.repository

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun getTasksForList(listId: Long): Flow<List<Task>>
    fun getDueTodayTasks(today: LocalDate): Flow<List<Task>>
    fun getDeletedTasks(): Flow<List<TaskWithListName>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun getOpenTasksDueBy(date: LocalDate): List<Task>
    suspend fun getOpenTasksWithDueTime(): List<Task>
    suspend fun getTaskIdsForList(taskListId: Long): List<Long>
    suspend fun insertTask(task: Task): Long
    /** Writes the user-editable fields only, leaving done state, timestamps and reminder state untouched. */
    suspend fun updateTaskDetails(task: Task)
    suspend fun toggleTaskDeleted(taskId: Long, isDeleted: Boolean)
    suspend fun toggleTaskDone(taskId: Long, isDone: Boolean, completedAt: Long?)
    suspend fun setReminderShown(taskId: Long, isShown: Boolean)
    suspend fun permanentlyDeleteTask(taskId: Long)
    suspend fun permanentlyDeleteAllDeletedTask()
    suspend fun toggleAllTasksInListDeleted(taskListId: Long, isDeleted: Boolean)
}
