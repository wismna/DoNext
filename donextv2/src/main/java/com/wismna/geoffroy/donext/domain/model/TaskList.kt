package com.wismna.geoffroy.donext.domain.model

data class TaskList(
    val id: Long? = null,
    val name: String,
    val isDeleted: Boolean,
    val order: Int
)
