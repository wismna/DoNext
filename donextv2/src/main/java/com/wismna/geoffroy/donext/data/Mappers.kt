package com.wismna.geoffroy.donext.data

import com.wismna.geoffroy.donext.data.entities.TaskEntity
import com.wismna.geoffroy.donext.data.entities.TaskListEntity
import com.wismna.geoffroy.donext.data.entities.TaskWithListNameEntity
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import java.time.LocalTime

private const val MINUTES_PER_HOUR = 60

fun TaskEntity.toDomain() = Task(
    id = id,
    name = name,
    taskListId = taskListId,
    description = description,
    isDone = isDone,
    isDeleted = isDeleted,
    dueDate = dueDate,
    dueTime = dueTimeMinutes?.toLocalTime(),
    priority = priority,
    createdAt = createdAt,
    completedAt = completedAt,
    isReminderShown = isReminderShown
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
    dueDate = dueDate,
    dueTimeMinutes = dueTime?.toMinutesOfDay(),
    createdAt = createdAt,
    completedAt = completedAt,
    isReminderShown = isReminderShown
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

fun LocalTime.toMinutesOfDay(): Int = hour * MINUTES_PER_HOUR + minute

fun Int.toLocalTime(): LocalTime = LocalTime.of(this / MINUTES_PER_HOUR, this % MINUTES_PER_HOUR)
