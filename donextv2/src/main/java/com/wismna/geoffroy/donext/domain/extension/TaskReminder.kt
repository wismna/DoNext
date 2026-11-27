package com.wismna.geoffroy.donext.domain.extension

import com.wismna.geoffroy.donext.domain.model.Task
import java.time.Clock
import java.time.Instant

/** The moment this task should notify, or null when it has no reminder to fire (done, deleted, or no due time). */
fun Task.reminderInstant(clock: Clock): Instant? {
    if (isDone || isDeleted) return null
    val date = dueDate ?: return null
    val time = dueTime ?: return null
    return date.atTime(time).atZone(clock.zone).toInstant()
}

/**
 * Resets the reminder after its due date or time was set. A reminder whose moment had already
 * passed at that point counts as shown, so saving a task never pops up an immediate notification.
 */
fun Task.withReminderReset(clock: Clock): Task {
    val reminderAt = reminderInstant(clock)
    return copy(isReminderShown = reminderAt != null && !reminderAt.isAfter(clock.instant()))
}
