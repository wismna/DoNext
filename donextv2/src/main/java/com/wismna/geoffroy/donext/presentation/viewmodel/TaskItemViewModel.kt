package com.wismna.geoffroy.donext.presentation.viewmodel

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

class TaskItemViewModel(task: Task) {
    val id: Long = task.id!!
    val name: String = task.name
    val description: String? = task.description
    val isDone: Boolean = task.isDone
    val priority: Priority = task.priority

    val today: LocalDate = LocalDate.now(ZoneOffset.UTC)

    val isOverdue: Boolean = task.dueDate?.let { millis ->
        val dueDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        dueDate.isBefore(today)
    } ?: false

    val dueDateText: String? = task.dueDate?.let { formatDueDate(it) }

    private fun formatDueDate(dueMillis: Long): String {
        val dueDate = Instant.ofEpochMilli(dueMillis).atZone(ZoneOffset.UTC).toLocalDate()

        return when {
            dueDate.isEqual(today) -> "Today"
            dueDate.isEqual(today.plusDays(1)) -> "Tomorrow"
            dueDate.isEqual(today.minusDays(1)) -> "Yesterday"
            dueDate.isAfter(today) && dueDate.isBefore(today.plusDays(7)) ->
                dueDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            else ->
                dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
        }
    }
}