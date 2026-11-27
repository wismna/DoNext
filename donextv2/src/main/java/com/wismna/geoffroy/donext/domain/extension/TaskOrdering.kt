package com.wismna.geoffroy.donext.domain.extension

import com.wismna.geoffroy.donext.domain.model.Task
import java.time.Clock
import java.time.LocalDate

private const val URGENT_BUCKET = 0
private const val REGULAR_BUCKET = 1

/**
 * Open tasks first, overdue and due-today ones on top; then by priority, nearest due date and time,
 * undated last. Done tasks follow, ordered the same way minus the urgency bucket.
 */
fun List<Task>.sortedForDisplay(clock: Clock): List<Task> {
    val today = LocalDate.now(clock)
    return sortedWith(
        compareBy<Task> { it.isDone }
            .thenBy { it.urgencyBucket(today) }
            .thenByDescending { it.priority.value }
            .thenBy(nullsLast()) { it.dueDate }
            .thenBy(nullsLast()) { it.dueTime }
    )
}

private fun Task.urgencyBucket(today: LocalDate): Int {
    val isUrgent = !isDone && dueDate != null && !dueDate.isAfter(today)
    return if (isUrgent) URGENT_BUCKET else REGULAR_BUCKET
}
