package com.wismna.geoffroy.donext.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasklist")
class TaskList {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "visible")
    var visible = true

    @JvmField
    @ColumnInfo(name = "displayorder")
    var order = 0

    //@ColumnInfo(name = "taskcount")
    @JvmField
    var taskCount = 0
}
