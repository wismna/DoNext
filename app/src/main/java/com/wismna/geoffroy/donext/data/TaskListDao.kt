package com.wismna.geoffroy.donext.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskListDao {
    @Insert
    fun createTaskList(taskList: TaskList?)

    @Update
    fun updateTaskList(taskList: TaskList?)

    @get:Query("SELECT *,(SELECT COUNT(*) FROM tasks WHERE tasks.list = tasklist._id) AS taskcount" +
            " FROM tasklist WHERE visible = 1 ORDER BY displayorder ASC ")
    val visibleTaskLists: LiveData<List<TaskList?>?>?

    @get:Query("SELECT *, (SELECT COUNT(*) FROM tasks WHERE tasks.list = tasklist._id AND (tasks.deleted = 1 OR tasks.done = 1)) AS taskcount" +
            " FROM tasklist WHERE visible = 0 OR taskcount > 0 ORDER BY displayorder ASC ")
    val invisibleTaskLists: LiveData<List<TaskList?>?>?
}
