package com.wismna.geoffroy.donext.data;

import org.joda.time.LocalDate;

import androidx.room.DatabaseView;

@DatabaseView("SELECT * FROM tasks WHERE todaydate = date('now','localtime')")
public class TodayTasksView extends Task {
    /*public long _id;
    public String name;
    public String description;
    public int cycle;
    public int priority;
    public boolean done;
    public boolean deleted;
    public int order;
    public int todayOrder;
    public long taskList;
    public LocalDate dueDate;*/
}
