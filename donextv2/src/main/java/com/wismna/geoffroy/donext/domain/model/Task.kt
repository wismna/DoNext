package com.wismna.geoffroy.donext.domain.model

data class Task(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val priority: Priority,
    val isDone: Boolean,
    val isDeleted: Boolean,
    val taskListId: Long,
    val dueDate: Long? = null
)
