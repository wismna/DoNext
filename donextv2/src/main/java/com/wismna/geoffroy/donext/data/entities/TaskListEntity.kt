package com.wismna.geoffroy.donext.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "display_order")
    val order: Int,
    @ColumnInfo(name = "deleted")
    val isDeleted: Boolean = false
)
