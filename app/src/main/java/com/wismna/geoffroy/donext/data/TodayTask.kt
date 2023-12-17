package com.wismna.geoffroy.donext.data

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

class TodayTask {
    @JvmField
    @PrimaryKey
    var _id: Long = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "todaydate")
    var todayDate: LocalDate? = null

    @JvmField
    @ColumnInfo(name = "tasklistname")
    var taskListName: String? = null
}
