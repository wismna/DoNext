package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class GetDigestTasksUseCaseTest {

    private val zone = ZoneId.of("America/Montreal")
    private val today = LocalDate.of(2026, 3, 10)
    private val clock = Clock.fixed(today.atTime(9, 0).atZone(zone).toInstant(), zone)

    private val taskRepository = mockk<TaskRepository>()
    private val useCase = GetDigestTasksUseCase(taskRepository, clock)

    private fun task(name: String, daysFromToday: Long, dueTime: LocalTime? = null) = Task(
        id = name.hashCode().toLong(),
        name = name,
        description = null,
        priority = Priority.NORMAL,
        isDone = false,
        isDeleted = false,
        taskListId = 1L,
        dueDate = today.plusDays(daysFromToday),
        dueTime = dueTime
    )

    @Test
    fun `queries open tasks due up to today`() = runTest {
        coEvery { taskRepository.getOpenTasksDueBy(any()) } returns emptyList()

        useCase()

        coVerify { taskRepository.getOpenTasksDueBy(today) }
    }

    @Test
    fun `keeps overdue tasks and untimed tasks due today, skips timed tasks due today`() = runTest {
        coEvery { taskRepository.getOpenTasksDueBy(any()) } returns listOf(
            task("Overdue timed", daysFromToday = -1, dueTime = LocalTime.of(8, 0)),
            task("Overdue untimed", daysFromToday = -3),
            task("Today untimed", daysFromToday = 0),
            task("Today timed", daysFromToday = 0, dueTime = LocalTime.of(15, 0))
        )

        val names = useCase().map { it.name }

        assertThat(names).containsExactly("Overdue untimed", "Overdue timed", "Today untimed").inOrder()
    }
}
