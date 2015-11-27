package com.wismna.geoffroy.donext.dao;

/**
 * Created by geoffroy on 15-11-25.
 */
public class Task {
    private long id;
    private String name;
    private String description;
    private int priority;
    private int cycle;
    private int done;
    private int deleted;
    private long taskList;
    private String taskListName;

    public enum TaskPriority {
        LOW(0),
        NORMAL(1),
        HIGH(2);

        private int value;

        private TaskPriority(int value) {
            this.value = value;
        }
    }

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public boolean isDone() {
        return done != 0;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public boolean isDeleted() {
        return deleted != 0;
    }

    public void setDeleted(int deleted) {
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
