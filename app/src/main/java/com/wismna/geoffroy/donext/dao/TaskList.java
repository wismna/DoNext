package com.wismna.geoffroy.donext.dao;

import android.support.annotation.NonNull;

/**
 * Created by geoffroy on 15-11-25.
 * Data access object class that represents a Task List
 */
public class TaskList {
    private long id;
    private String name;
    private long taskCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String comment) {
        this.name = comment;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
