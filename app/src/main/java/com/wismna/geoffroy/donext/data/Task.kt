package com.wismna.geoffroy.donext.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity(tableName = "tasks", indices = [Index("list")], foreignKeys = [ForeignKey(entity = TaskList::class, parentColumns = ["_id"], childColumns = ["list"])])
open class Task {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "description")
    var description: String? = null

    @JvmField
    @ColumnInfo(name = "cycle")
    var cycle = 0

    @JvmField
    @ColumnInfo(name = "priority")
    var priority = 1

    @JvmField
    @ColumnInfo(name = "done")
    var done = false

    @JvmField
    @ColumnInfo(name = "deleted")
    var deleted = false

    @JvmField
    @ColumnInfo(name = "displayorder")
    var order = 0

    @JvmField
    @ColumnInfo(name = "todayorder")
    var todayOrder = 0

    @JvmField
    @ColumnInfo(name = "list")
    var taskList: Long = 0

    @JvmField
    @ColumnInfo(name = "duedate")
    var dueDate: LocalDate? = null

    @JvmField
    @ColumnInfo(name = "todaydate")
    var todayDate: LocalDate? = null
}
