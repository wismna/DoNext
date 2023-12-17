package com.wismna.geoffroy.donext.data;

import org.joda.time.LocalDate;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        indices = {@Index("list")},
        foreignKeys = @ForeignKey(entity = TaskList.class,
            parentColumns = "_id",
            childColumns = "list"))
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "cycle")
    public int cycle = 0;

    @ColumnInfo(name = "priority")
    public int priority = 1;

    @ColumnInfo(name = "done")
    public boolean done = false;

    @ColumnInfo(name = "deleted")
    public boolean deleted = false;

    @ColumnInfo(name = "displayorder")
    public int order;

    @ColumnInfo(name = "todayorder")
    public int todayOrder;

    @ColumnInfo(name = "list")
    public long taskList;

    @ColumnInfo(name = "duedate")
    public LocalDate dueDate;

    @ColumnInfo(name = "todaydate")
    public LocalDate todayDate;
}
