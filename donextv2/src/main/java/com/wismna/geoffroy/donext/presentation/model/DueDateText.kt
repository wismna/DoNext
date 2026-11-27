package com.wismna.geoffroy.donext.presentation.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** "Today", "Tomorrow", "Yesterday", a weekday within the next week, otherwise a medium date. */
fun formatDueDate(dueDate: LocalDate, today: LocalDate): String =
    when {
        dueDate.isEqual(today) -> "Today"
        dueDate.isEqual(today.plusDays(1)) -> "Tomorrow"
        dueDate.isEqual(today.minusDays(1)) -> "Yesterday"
        dueDate.isAfter(today) && dueDate.isBefore(today.plusDays(7)) ->
            dueDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        else ->
            dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
    }
