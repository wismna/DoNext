package com.wismna.geoffroy.donext.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val cycles: Int = 0,
    @ColumnInfo(name = "done")
    val isDone: Boolean = false,
    @ColumnInfo(name = "deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "task_list_id")
    val taskListId: Long,
    @ColumnInfo(name = "update_date")
    val updateDate: Long = System.currentTimeMillis()
)
