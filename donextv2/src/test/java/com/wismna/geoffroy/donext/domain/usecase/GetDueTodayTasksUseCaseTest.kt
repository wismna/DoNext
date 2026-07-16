package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class GetDueTodayTasksUseCaseTest {

    @Test
    fun `computes today's window using the clock's own zone, not UTC`() {
        // Paris in summer is UTC+2: local midnight for this date is 22:00 UTC
        // the *previous* day. A window anchored to UTC midnight would miss it.
        val zone = ZoneId.of("Europe/Paris")
        val today = LocalDate.of(2026, 7, 16)
        val clock = Clock.fixed(today.atStartOfDay(zone).plusHours(10).toInstant(), zone)

        val repository = mockk<TaskRepository>()
        every { repository.getDueTodayTasks(any(), any()) } returns flowOf(emptyList())

        GetDueTodayTasksUseCase(repository, clock).invoke()

        val expectedStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        verify { repository.getDueTodayTasks(expectedStart, expectedEnd) }
    }

    @Test
    fun `window covers exactly local midnight to the millisecond before the next local midnight`() {
        val zone = ZoneId.of("America/Montreal")
        val today = LocalDate.of(2026, 1, 5)
        val clock = Clock.fixed(today.atStartOfDay(zone).plusHours(6).toInstant(), zone)

        val repository = mockk<TaskRepository>()
        every { repository.getDueTodayTasks(any(), any()) } returns flowOf(emptyList())

        GetDueTodayTasksUseCase(repository, clock).invoke()

        val expectedStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val expectedEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        assertThat(expectedEnd - expectedStart).isEqualTo(24L * 60 * 60 * 1000 - 1)

        verify { repository.getDueTodayTasks(expectedStart, expectedEnd) }
    }

    @Test
    fun `passes through the tasks emitted by the repository`() = runTest {
        val zone = ZoneId.systemDefault()
        val clock = Clock.fixed(LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant(), zone)
        val tasks = listOf(
            Task(
                id = 1L,
                taskListId = 1L,
                name = "Task",
                description = "",
                priority = Priority.NORMAL,
                isDone = false,
                isDeleted = false
            )
        )

        val repository = mockk<TaskRepository>()
        every { repository.getDueTodayTasks(any(), any()) } returns flowOf(tasks)

        val result = GetDueTodayTasksUseCase(repository, clock).invoke().first()

        assertThat(result).isEqualTo(tasks)
    }
}
