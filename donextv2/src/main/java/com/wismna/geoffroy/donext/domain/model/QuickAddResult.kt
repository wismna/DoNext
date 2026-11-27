package com.wismna.geoffroy.donext.domain.model

import java.time.LocalDate
import java.time.LocalTime

/** What a quick-add text means: the title left once shortcuts are removed, and the values they set. */
data class QuickAddResult(
    val title: String,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: Priority? = null,
    val taskListId: Long? = null
) {
    val hasShortcuts: Boolean
        get() = dueDate != null || dueTime != null || priority != null || taskListId != null
}
