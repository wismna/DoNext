package com.wismna.geoffroy.donext.domain.model

import java.time.Instant

data class Task(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val cycle: Int,
    val priority: Int,
    val order: Int,
    val isDone: Boolean,
    val isDeleted: Boolean,
    val taskListId: Long,
    val dueDate: Instant? = null
)
