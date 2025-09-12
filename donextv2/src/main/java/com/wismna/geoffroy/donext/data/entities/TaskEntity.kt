package com.wismna.geoffroy.donext.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val cycle: Int = 0,
    val priority: Int,
    @ColumnInfo(name = "display_order")
    val order: Int,
    @ColumnInfo(name = "done")
    val isDone: Boolean = false,
    @ColumnInfo(name = "deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "task_list_id")
    val taskListId: Long,
    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null
)
