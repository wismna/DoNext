package com.wismna.geoffroy.donext.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskWithListNameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE task_list_id = :listId AND deleted = 0 ORDER BY done ASC, priority DESC")
    fun getTasksForList(listId: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE due_date BETWEEN :todayStart AND :todayEnd AND deleted = 0 AND done = 0 
        ORDER BY done ASC, priority DESC
    """)
    fun getDueTodayTasks(todayStart: Long, todayEnd: Long): Flow<List<TaskEntity>>

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

    @Query("DELETE FROM tasks WHERE deleted = 1")
    suspend fun permanentDeleteAllDeletedTasks()
}