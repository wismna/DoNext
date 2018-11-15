package com.wismna.geoffroy.donext.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasklist")
public class TaskList {

    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "visible")
    public boolean visible = true;

    @ColumnInfo(name = "displayorder")
    public int order;

    //@ColumnInfo(name = "taskcount")
    public int taskCount;
}
