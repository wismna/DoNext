package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
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

class GetTaskListsWithOverdueUseCaseTest {

    @Test
    fun `computes the overdue cutoff at local midnight, not UTC midnight`() {
        // Paris in summer is UTC+2: local midnight for this date is 22:00 UTC
        // the *previous* day. A cutoff anchored to UTC midnight would flag
        // today's tasks as overdue a full day too early.
        val zone = ZoneId.of("Europe/Paris")
        val today = LocalDate.of(2026, 7, 16)
        val clock = Clock.fixed(today.atStartOfDay(zone).plusHours(10).toInstant(), zone)

        val repository = mockk<TaskRepository>()
        every { repository.getTaskListsWithOverdue(any()) } returns flowOf(emptyList())

        GetTaskListsWithOverdueUseCase(repository, clock).invoke()

        val expectedCutoff = today.atStartOfDay(zone).toInstant().toEpochMilli()
        verify { repository.getTaskListsWithOverdue(expectedCutoff) }
    }

    @Test
    fun `passes through the lists emitted by the repository`() = runTest {
        val zone = ZoneId.systemDefault()
        val clock = Clock.fixed(LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant(), zone)
        val lists = listOf(TaskListWithOverdue(id = 1L, name = "Work", overdueCount = 2))

        val repository = mockk<TaskRepository>()
        every { repository.getTaskListsWithOverdue(any()) } returns flowOf(lists)

        val result = GetTaskListsWithOverdueUseCase(repository, clock).invoke().first()

        assertThat(result).isEqualTo(lists)
    }
}
