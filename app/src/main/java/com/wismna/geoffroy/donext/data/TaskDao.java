package com.wismna.geoffroy.donext.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {

    @Insert()
    void createTask (Task task);

    @Update()
    void updateTask (Task task);

    @Query("SELECT " +
            "tasks._id," +
            "tasks.name," +
            "tasks.todaydate," +
            "tasklist.name AS tasklistname " +
            " FROM tasks" +
            " LEFT JOIN tasklist ON tasks.list = tasklist._id" +
            " WHERE tasks.done = 0 AND tasks.deleted = 0")
    LiveData<List<TodayTask>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE list = :id AND done = 0 AND deleted = 0")
    LiveData<List<Task>> getAllTasksFromList(long id);

    @Query("SELECT * FROM tasks WHERE list = :id AND done = 1 OR deleted = 1")
    LiveData<List<Task>> getAllTasksFromHistoryList(long id);

    // TODO: replace query with view
    //@Query("SELECT * FROM todaytasksview WHERE done = 0 AND deleted = 0")
    @Query("SELECT * FROM tasks WHERE todaydate = date('now','localtime') AND done = 0 AND deleted = 0")
    LiveData<List<Task>> getTodayTasks();

    // TODO: replace this with item count from recycle view
    @Query("SELECT MAX(displayorder) FROM tasks WHERE list = :id")
    int getMaxOrder(long id);

    @Query("UPDATE tasks SET displayorder = displayorder - 1" +
            " WHERE displayorder > (SELECT displayorder FROM tasks WHERE _id = :id)")
    void updateRemainingRowsOrder(long id);

    @Query("UPDATE tasks SET todayorder = todayorder - 1" +
            " WHERE todayorder > (SELECT todayorder FROM tasks WHERE _id = :id)")
    void updateRemainingRowsTodayOrder(long id);

    @Query("UPDATE tasks SET deleted = 1 WHERE list = :id")
    void deleteAllTasks(long id);
}
