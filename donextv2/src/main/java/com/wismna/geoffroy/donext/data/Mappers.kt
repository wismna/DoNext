package com.wismna.geoffroy.donext.data

import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import java.time.Instant

fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    isDone = isDone,
    taskListId = taskListId,
    description = description,
    cycles = cycles,
    isDeleted = isDeleted,
    updateDate = Instant.ofEpochMilli(updateDate)
)

fun Task.toEntity() = TaskEntity(
    id = id,
    name = name,
    isDone = isDone,
    taskListId = taskListId,
    description = description,
    cycles = cycles,
    isDeleted = isDeleted,
    updateDate = updateDate.toEpochMilli()
)

fun TaskListEntity.toDomain() = TaskList(
    id = id,
    name = name,
    isDeleted = isDeleted
)

fun TaskList.toEntity() = TaskListEntity(
    id = id,
    name = name,
    isDeleted = isDeleted
)
