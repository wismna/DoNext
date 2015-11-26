package com.wismna.geoffroy.donext.dao;

/**
 * Created by geoffroy on 15-11-25.
 */
public class Task {
    private long id;
    private String name;
    private String description;
    private int cycle;
    private boolean deleted;
    private long taskList;
    private String taskListName;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getTaskList() {
        return taskList;
    }

    public void setTaskList(long taskList) {
        this.taskList = taskList;
    }

    public String getTaskListName() {
        return taskListName;
    }

    public void setTaskListName(String taskListName) {
        this.taskListName = taskListName;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return name;
    }
}
