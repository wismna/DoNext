package com.wismna.geoffroy.donext.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    fun createTask(task: Task?)

    @Update
    fun updateTask(task: Task?)

    @get:Query("SELECT " +
            "tasks._id," +
            "tasks.name," +
            "tasks.todaydate," +
            "tasklist.name AS tasklistname " +
            " FROM tasks" +
            " LEFT JOIN tasklist ON tasks.list = tasklist._id" +
            " WHERE tasks.done = 0 AND tasks.deleted = 0")
    val allTasks: LiveData<List<TodayTask?>?>?

    @Query("SELECT * FROM tasks WHERE list = :id AND done = 0 AND deleted = 0")
    fun getAllTasksFromList(id: Long): LiveData<List<Task?>?>?

    @Query("SELECT * FROM tasks WHERE list = :id AND done = 1 OR deleted = 1")
    fun getAllTasksFromHistoryList(id: Long): LiveData<List<Task?>?>?

    @get:Query("SELECT * FROM tasks WHERE todaydate = date('now','localtime') AND done = 0 AND deleted = 0")
    val todayTasks: LiveData<List<Task?>?>?

    // TODO: replace this with item count from recycle view
    @Query("SELECT MAX(displayorder) FROM tasks WHERE list = :id")
    fun getMaxOrder(id: Long): Int

    @Query("UPDATE tasks SET displayorder = displayorder - 1" +
            " WHERE displayorder > (SELECT displayorder FROM tasks WHERE _id = :id)")
    fun updateRemainingRowsOrder(id: Long)

    @Query("UPDATE tasks SET todayorder = todayorder - 1" +
            " WHERE todayorder > (SELECT todayorder FROM tasks WHERE _id = :id)")
    fun updateRemainingRowsTodayOrder(id: Long)

    @Query("UPDATE tasks SET deleted = 1 WHERE list = :id")
    fun deleteAllTasks(id: Long)
}
