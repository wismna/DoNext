package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists WHERE deleted = 0 ORDER BY display_order ASC")
    fun getTaskLists(): Flow<List<TaskListEntity>>

    @Query("""
        SELECT 
          tl.id AS id,
          tl.name AS name,
          COALESCE(SUM(
            CASE 
              WHEN t.done = 0 
               AND t.due_date IS NOT NULL 
               AND t.due_date < :nowMillis
              THEN 1 
              ELSE 0 
            END
          ), 0) AS overdueCount
        FROM task_lists tl
        LEFT JOIN tasks t ON t.task_list_id = tl.id
        WHERE tl.deleted = 0
        GROUP BY tl.id
    """)
    fun getTaskListsWithOverdue(nowMillis: Long): Flow<List<TaskListWithOverdue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskList(taskList: TaskListEntity)

    @Update
    suspend fun updateTaskList(taskList: TaskListEntity)

    @Query("UPDATE task_lists SET deleted = :isDeleted WHERE id = :listId")
    suspend fun deleteTaskList(listId: Long, isDeleted: Boolean)
}