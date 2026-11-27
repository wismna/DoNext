package com.wismna.geoffroy.donext.presentation.model

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TaskItemUiState(
    task: Task,
    private val clock: Clock = Clock.systemDefaultZone()) {
    val id: Long = task.id!!
    val name: String = task.name
    val description: String? = task.description
    val isDone: Boolean = task.isDone
    val isDeleted: Boolean = task.isDeleted
    val priority: Priority = task.priority

    val today: LocalDate = LocalDate.now(clock)

    val isOverdue: Boolean = task.dueDate?.isBefore(today) ?: false

    val dueDateText: String? = task.dueDate?.let { dueDate ->
        val dateText = formatDueDate(dueDate, today)
        val timeText = task.dueTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        if (timeText == null) dateText else "$dateText · $timeText"
    }
}
