package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wismna.geoffroy.donext.data.entities.TaskEntity

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE task_list_id = :listId")
    suspend fun getTasksForList(listId: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET done = :done, update_date = :updateDate WHERE id = :taskId")
    suspend fun markTaskDone(taskId: Long, done: Boolean, updateDate: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET deleted = :deleted, update_date = :updateDate WHERE id = :taskId")
    suspend fun markTaskDeleted(taskId: Long, deleted: Boolean, updateDate: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET cycles = cycles + 1, update_date = :updateDate WHERE id = :taskId")
    suspend fun increaseCycle(taskId: Long, updateDate: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET deleted = :deleted WHERE id = :taskListId")
    suspend fun deleteAllTasksFromList(taskListId: Long, deleted: Boolean)
}