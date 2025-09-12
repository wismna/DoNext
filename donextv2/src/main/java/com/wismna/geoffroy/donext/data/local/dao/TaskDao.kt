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
    @Query("SELECT * FROM tasks WHERE task_list_id = :listId ORDER BY display_order ASC")
    fun getTasksForList(listId: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET done = :done WHERE id = :taskId")
    suspend fun markTaskDone(taskId: Long, done: Boolean)

    @Query("UPDATE tasks SET deleted = :deleted WHERE id = :taskId")
    suspend fun markTaskDeleted(taskId: Long, deleted: Boolean)

    @Query("UPDATE tasks SET cycle = cycle + 1 WHERE id = :taskId")
    suspend fun increaseCycle(taskId: Long)

    @Query("UPDATE tasks SET deleted = :deleted WHERE id = :taskListId")
    suspend fun deleteAllTasksFromList(taskListId: Long, deleted: Boolean)
}