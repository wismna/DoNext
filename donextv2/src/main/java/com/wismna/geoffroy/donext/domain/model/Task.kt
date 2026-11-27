package com.wismna.geoffroy.donext.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val priority: Priority,
    val isDone: Boolean,
    val isDeleted: Boolean,
    val taskListId: Long,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val createdAt: Long? = null,
    val completedAt: Long? = null,
    val isReminderShown: Boolean = false
)
