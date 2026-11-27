package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class GetTasksForListUseCaseTest {

    @Test
    fun `puts the overdue task above a higher priority future task`() = runTest {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 3, 10)
        val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
        fun task(name: String, priority: Priority, dueDate: LocalDate) = Task(
            id = name.hashCode().toLong(), taskListId = 1L, name = name, description = null,
            priority = priority, isDone = false, isDeleted = false,
            dueDate = dueDate
        )
        val repository = mockk<TaskRepository>()
        every { repository.getTasksForList(1L) } returns flowOf(
            listOf(
                task("Future high", Priority.HIGH, today.plusDays(2)),
                task("Overdue low", Priority.LOW, today.minusDays(1))
            )
        )

        val names = GetTasksForListUseCase(repository, clock)(1L).first().map { it.name }

        assertThat(names).containsExactly("Overdue low", "Future high").inOrder()
    }
}
