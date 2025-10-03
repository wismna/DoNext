package com.wismna.geoffroy.donext.data.entities

import androidx.room.Embedded

data class TaskWithListNameEntity(
    @Embedded val task: TaskEntity,
    val listName: String
)