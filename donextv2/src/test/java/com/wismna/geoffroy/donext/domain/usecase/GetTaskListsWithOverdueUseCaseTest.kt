package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
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
    fun `uses the local calendar day as the overdue cutoff`() {
        // 00:30 in Paris is still the previous day in UTC
        val zone = ZoneId.of("Europe/Paris")
        val today = LocalDate.of(2026, 7, 16)
        val clock = Clock.fixed(today.atTime(0, 30).atZone(zone).toInstant(), zone)

        val repository = mockk<TaskListRepository>()
        every { repository.getTaskListsWithOverdue(any()) } returns flowOf(emptyList())

        GetTaskListsWithOverdueUseCase(repository, clock).invoke()

        verify { repository.getTaskListsWithOverdue(today) }
    }

    @Test
    fun `passes through the lists emitted by the repository`() = runTest {
        val zone = ZoneId.systemDefault()
        val clock = Clock.fixed(LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant(), zone)
        val lists = listOf(TaskListWithOverdue(id = 1L, name = "Work", overdueCount = 2))

        val repository = mockk<TaskListRepository>()
        every { repository.getTaskListsWithOverdue(any()) } returns flowOf(lists)

        val result = GetTaskListsWithOverdueUseCase(repository, clock).invoke().first()

        assertThat(result).isEqualTo(lists)
    }
}
