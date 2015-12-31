package com.wismna.geoffroy.donext.dao;

/**
 * Created by geoffroy on 15-11-25.
 * Data access object class that represents a Task List
 */
public class TaskList {
    private long id;
    private String name;
    private long taskCount;
    private int order;

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return name;
    }
}
