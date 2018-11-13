package com.wismna.geoffroy.donext.dao;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

/**
 * Created by geoffroy on 15-11-25.
 * Data access object class that represents a Task
 */
public class Task {
    private long id;
    private String name;
    private String description;
    private int priority;
    private int cycle;
    private int done;
    private int deleted;
    private int order;
    private int todayOrder;
    private long taskList;
    private String taskListName;
    private LocalDate dueDate;
    private LocalDate todayDate;

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

    public void setDone(int done) {
        this.done = done;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public long getTaskListId() {
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

    public void setDueDate(String dueDate) {
        try {
            this.dueDate = LocalDate.parse(dueDate);
        }
        catch (Exception e){
            this.dueDate = null;
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getDone() {
        return done;
    }

    public void setTodayDate(String todayDate) {
        try {
            this.todayDate = LocalDate.parse(todayDate);
        }
        catch (Exception e){
            this.todayDate = null;
        }
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    public int getTodayOrder() {
        return todayOrder;
    }
    public void setTodayOrder(int todayOrder) {
        this.todayOrder = todayOrder;
    }

    public boolean isToday() {
        return todayDate != null && todayDate.isEqual(LocalDate.now());
    }

    public boolean isHistory () {
        return getDone() == 1 || getDeleted() == 1;
    }

    // Will be used by the ArrayAdapter in the ListView
    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
