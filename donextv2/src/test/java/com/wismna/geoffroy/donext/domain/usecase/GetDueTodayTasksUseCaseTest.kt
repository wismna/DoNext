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
import java.time.LocalTime
import java.time.ZoneId

class GetDueTodayTasksUseCaseTest {

    @Test
    fun `asks for the local calendar day, not the UTC one`() {
        // 00:30 in Paris is still the previous day in UTC
        val zone = ZoneId.of("Europe/Paris")
        val today = LocalDate.of(2026, 7, 16)
        val clock = Clock.fixed(today.atTime(0, 30).atZone(zone).toInstant(), zone)

        val repository = mockk<TaskRepository>()
        every { repository.getDueTodayTasks(any()) } returns flowOf(emptyList())

        GetDueTodayTasksUseCase(repository, clock).invoke()

        verify { repository.getDueTodayTasks(today) }
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
        every { repository.getDueTodayTasks(any()) } returns flowOf(tasks)

        val result = GetDueTodayTasksUseCase(repository, clock).invoke().first()

        assertThat(result).isEqualTo(tasks)
    }

    @Test
    fun `orders due today tasks by priority then due time`() = runTest {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.of(2026, 1, 1)
        val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
        fun task(name: String, priority: Priority, time: LocalTime?) = Task(
            id = name.hashCode().toLong(), taskListId = 1L, name = name, description = "",
            priority = priority, isDone = false, isDeleted = false, dueDate = today, dueTime = time
        )
        val repository = mockk<TaskRepository>()
        every { repository.getDueTodayTasks(any()) } returns flowOf(
            listOf(
                task("Normal late", Priority.NORMAL, LocalTime.of(18, 0)),
                task("Normal early", Priority.NORMAL, LocalTime.of(8, 0)),
                task("High", Priority.HIGH, null)
            )
        )

        val names = GetDueTodayTasksUseCase(repository, clock).invoke().first().map { it.name }

        assertThat(names).containsExactly("High", "Normal early", "Normal late").inOrder()
    }
}
