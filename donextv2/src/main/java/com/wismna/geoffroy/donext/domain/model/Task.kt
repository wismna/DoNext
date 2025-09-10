package com.wismna.geoffroy.donext.domain.model

import java.time.Instant

data class Task(
    val id: Long,
    val name: String,
    val description: String,
    val cycles: Int,
    val isDone: Boolean,
    val isDeleted: Boolean,
    val taskListId: Long,
    val updateDate: Instant = Instant.now()
)
