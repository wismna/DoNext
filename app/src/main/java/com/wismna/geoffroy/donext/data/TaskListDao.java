package com.wismna.geoffroy.donext.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskListDao {

    @Insert()
    void createTaskList(TaskList taskList);

    @Update()
    void updateTaskList(TaskList taskList);

    @Query("SELECT *,(SELECT COUNT(*) FROM tasks WHERE tasks.list = tasklist._id) AS taskcount"  +
            " FROM tasklist WHERE visible = 1 ORDER BY displayorder ASC ")
    LiveData<List<TaskList>> getVisibleTaskLists();

    @Query("SELECT *, (SELECT COUNT(*) FROM tasks WHERE tasks.list = tasklist._id AND (tasks.deleted = 1 OR tasks.done = 1)) AS taskcount" +
            " FROM tasklist WHERE visible = 0 OR taskcount > 0 ORDER BY displayorder ASC ")
    LiveData<List<TaskList>> getInvisibleTaskLists();
}
