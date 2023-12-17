package com.wismna.geoffroy.donext.data;

import org.joda.time.LocalDate;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class TodayTask {
    @PrimaryKey()
    public long _id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "todaydate")
    public LocalDate todayDate;

    @ColumnInfo(name = "tasklistname")
    public String taskListName;
}
