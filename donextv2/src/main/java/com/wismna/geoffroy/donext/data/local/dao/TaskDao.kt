package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskWithListNameEntity
import com.wismna.geoffroy.donext.domain.model.Priority
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE task_list_id = :listId AND deleted = 0 ORDER BY done ASC, priority DESC")
    fun getTasksForList(listId: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE due_date = :today AND deleted = 0 AND done = 0 
        ORDER BY done ASC, priority DESC
    """)
    fun getDueTodayTasks(today: LocalDate): Flow<List<TaskEntity>>

    @Query("""
        SELECT t.*, l.name AS listName
        FROM tasks t
        INNER JOIN task_lists l ON t.task_list_id = l.id
        WHERE t.deleted = 1
        ORDER BY l.name
    """)
    fun getDeletedTasksWithListName(): Flow<List<TaskWithListNameEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE done = 0 AND deleted = 0 AND due_date IS NOT NULL AND due_date <= :date")
    suspend fun getOpenTasksDueBy(date: LocalDate): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE done = 0 AND deleted = 0 AND due_date IS NOT NULL AND due_time_minutes IS NOT NULL")
    suspend fun getOpenTasksWithDueTime(): List<TaskEntity>

    @Query("SELECT id FROM tasks WHERE task_list_id = :taskListId")
    suspend fun getTaskIdsForList(taskListId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("""
        UPDATE tasks
        SET task_list_id = :taskListId, name = :name, description = :description, priority = :priority,
            due_date = :dueDate, due_time_minutes = :dueTimeMinutes
        WHERE id = :taskId
    """)
    suspend fun updateTaskDetails(
        taskId: Long,
        taskListId: Long,
        name: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDate?,
        dueTimeMinutes: Int?
    )

    @Query("UPDATE tasks SET done = :done, completed_at = :completedAt WHERE id = :taskId")
    suspend fun toggleTaskDone(taskId: Long, done: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET reminder_shown = :isShown WHERE id = :taskId")
    suspend fun setReminderShown(taskId: Long, isShown: Boolean)

    @Query("UPDATE tasks SET deleted = :deleted WHERE id = :taskId")
    suspend fun toggleTaskDeleted(taskId: Long, deleted: Boolean)

    @Query("UPDATE tasks SET deleted = :deleted WHERE task_list_id = :taskListId")
    suspend fun toggleAllTasksFromListDeleted(taskListId: Long, deleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun permanentDeleteTask(taskId: Long)

    @Query("DELETE FROM tasks WHERE deleted = 1")
    suspend fun permanentDeleteAllDeletedTasks()
}