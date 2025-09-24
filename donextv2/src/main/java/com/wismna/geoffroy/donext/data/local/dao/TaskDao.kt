package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE task_list_id = :listId AND deleted = 0 ORDER BY done ASC, priority DESC")
    fun getTasksForList(listId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE deleted = 1")
    suspend fun getDeletedTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET done = :done WHERE id = :taskId")
    suspend fun toggleTaskDone(taskId: Long, done: Boolean)

    @Query("UPDATE tasks SET deleted = :deleted WHERE id = :taskId")
    suspend fun toggleTaskDeleted(taskId: Long, deleted: Boolean)

    @Query("UPDATE tasks SET deleted = :deleted WHERE task_list_id = :taskListId")
    suspend fun toggleAllTasksFromListDeleted(taskListId: Long, deleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun permanentDeleteTask(taskId: Long)
}