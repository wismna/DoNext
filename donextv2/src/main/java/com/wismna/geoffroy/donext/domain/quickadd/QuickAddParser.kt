package com.wismna.geoffroy.donext.domain.quickadd

import com.wismna.geoffroy.donext.domain.model.QuickAddResult
import com.wismna.geoffroy.donext.domain.model.TaskList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Turns "Call dentist tomorrow 9am !high #Work" into a title and the values its shortcuts set (English and French).
 * Only the first shortcut of each kind is used; anything not recognized stays in the title.
 */
class QuickAddParser @Inject constructor() {

    fun parse(text: String, now: LocalDateTime, taskLists: List<TaskList>): QuickAddResult {
        val tokens = QuickAddTokens(text)
        val taskListId = ListMatcher.find(tokens, taskLists)?.also { tokens.consume(it.range) }?.value
        val priority = PriorityMatcher.find(tokens)?.also { tokens.consume(it.range) }?.value
        val date = DateMatcher.find(tokens, now.toLocalDate())?.also { tokens.consume(it.range) }?.value
        val time = TimeMatcher.find(tokens)?.also { tokens.consume(it.range) }
        val schedule = resolveSchedule(tokens, date, time, now)

        return QuickAddResult(
            title = tokens.remainingText(),
            dueDate = schedule.date,
            dueTime = schedule.time,
            priority = priority,
            taskListId = taskListId
        )
    }

    /**
     * A time alone means today if it is still ahead, otherwise tomorrow. A time already past for a task due today
     * would never notify, so it is not taken as a shortcut and stays in the title.
     */
    private fun resolveSchedule(
        tokens: QuickAddTokens,
        date: LocalDate?,
        time: QuickAddMatch<LocalTime>?,
        now: LocalDateTime
    ): Schedule {
        if (time == null) return Schedule(date, null)
        val today = now.toLocalDate()
        val isAheadToday = time.value.isAfter(now.toLocalTime())
        val dueDate = date ?: if (isAheadToday) today else today.plusDays(1)
        if (dueDate == today && !isAheadToday) {
            tokens.release(time.range)
            return Schedule(date, null)
        }
        return Schedule(dueDate, time.value)
    }

    private data class Schedule(val date: LocalDate?, val time: LocalTime?)
}
