package com.wismna.geoffroy.donext.data

import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import java.time.Instant

fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    taskListId = taskListId,
    description = description,
    cycle = cycle,
    isDone = isDone,
    isDeleted = isDeleted,
    dueDate = if (dueDate == null) null else Instant.ofEpochMilli(dueDate),
    priority = priority,
    order = order
)

fun Task.toEntity() = TaskEntity(
    id = id ?: 0,
    name = name,
    taskListId = taskListId,
    description = description,
    cycle = cycle,
    priority = priority,
    order = order,
    isDone = isDone,
    isDeleted = isDeleted,
    dueDate = dueDate?.toEpochMilli()
)

fun TaskListEntity.toDomain() = TaskList(
    id = id,
    name = name,
    isDeleted = isDeleted,
    order = order
)

fun TaskList.toEntity() = TaskListEntity(
    id = id,
    name = name,
    isDeleted = isDeleted,
    order = order
)
