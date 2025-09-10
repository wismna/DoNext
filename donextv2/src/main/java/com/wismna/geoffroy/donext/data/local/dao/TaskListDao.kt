package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists WHERE isDeleted = 0")
    fun getTaskLists(): Flow<List<TaskListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskList(taskList: TaskListEntity)

    @Update
    suspend fun updateTaskList(taskList: TaskListEntity)

    @Query("UPDATE task_lists SET isDeleted = :isDeleted WHERE id = :listId")
    suspend fun deleteTaskList(listId: Long, isDeleted: Boolean)
}