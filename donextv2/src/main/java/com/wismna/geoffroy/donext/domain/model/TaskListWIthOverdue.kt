package com.wismna.geoffroy.donext.domain.model

data class TaskListWithOverdue(
    val id: Long,
    val name: String,
    val overdueCount: Int
)