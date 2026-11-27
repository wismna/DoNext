package com.wismna.geoffroy.donext.domain.extension

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TaskOrderingTest {

    private val zone = ZoneId.of("America/Montreal")
    private val today = LocalDate.of(2026, 3, 10)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)

    private fun task(
        name: String,
        priority: Priority = Priority.NORMAL,
        daysFromToday: Long? = null,
        dueTime: LocalTime? = null,
        isDone: Boolean = false
    ) = Task(
        id = name.hashCode().toLong(),
        name = name,
        description = null,
        priority = priority,
        isDone = isDone,
        isDeleted = false,
        taskListId = 1L,
        dueDate = daysFromToday?.let { today.plusDays(it) },
        dueTime = dueTime
    )

    private fun List<Task>.names() = sortedForDisplay(clock).map { it.name }

    @Test
    fun `overdue and due today tasks come before higher priority future tasks`() {
        val tasks = listOf(
            task("Future high", Priority.HIGH, daysFromToday = 3),
            task("Today low", Priority.LOW, daysFromToday = 0),
            task("Overdue normal", Priority.NORMAL, daysFromToday = -2)
        )

        assertThat(tasks.names()).containsExactly("Overdue normal", "Today low", "Future high").inOrder()
    }

    @Test
    fun `within the same bucket tasks are sorted by priority first`() {
        val tasks = listOf(
            task("Future low soon", Priority.LOW, daysFromToday = 1),
            task("Future high later", Priority.HIGH, daysFromToday = 10)
        )

        assertThat(tasks.names()).containsExactly("Future high later", "Future low soon").inOrder()
    }

    @Test
    fun `same priority tasks are sorted by nearest due date with undated last`() {
        val tasks = listOf(
            task("Undated"),
            task("In five days", daysFromToday = 5),
            task("Tomorrow", daysFromToday = 1)
        )

        assertThat(tasks.names()).containsExactly("Tomorrow", "In five days", "Undated").inOrder()
    }

    @Test
    fun `same day tasks are sorted by due time with untimed last`() {
        val tasks = listOf(
            task("All day", daysFromToday = 2),
            task("Afternoon", daysFromToday = 2, dueTime = LocalTime.of(15, 0)),
            task("Morning", daysFromToday = 2, dueTime = LocalTime.of(8, 0))
        )

        assertThat(tasks.names()).containsExactly("Morning", "Afternoon", "All day").inOrder()
    }

    @Test
    fun `done tasks always come last, even when overdue`() {
        val tasks = listOf(
            task("Done overdue high", Priority.HIGH, daysFromToday = -1, isDone = true),
            task("Open undated low", Priority.LOW)
        )

        assertThat(tasks.names()).containsExactly("Open undated low", "Done overdue high").inOrder()
    }
}
