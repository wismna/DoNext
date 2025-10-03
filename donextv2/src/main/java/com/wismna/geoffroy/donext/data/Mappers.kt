package com.wismna.geoffroy.donext.data

import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.entities.TaskWithListNameEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskWithListName

fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    taskListId = taskListId,
    description = description,
    isDone = isDone,
    isDeleted = isDeleted,
    dueDate = dueDate,
    priority = priority,
)
fun TaskWithListNameEntity.toDomain(): TaskWithListName {
    return TaskWithListName(
        task = task.toDomain(),
        listName = listName
    )
}

fun Task.toEntity() = TaskEntity(
    id = id ?: 0,
    name = name,
    taskListId = taskListId,
    description = description,
    priority = priority,
    isDone = isDone,
    isDeleted = isDeleted,
    dueDate = dueDate
)

fun TaskListEntity.toDomain() = TaskList(
    id = id,
    name = name,
    isDeleted = isDeleted,
    order = order
)

fun TaskList.toEntity() = TaskListEntity(
    id = id ?: 0,
    name = name,
    isDeleted = isDeleted,
    order = order
)
