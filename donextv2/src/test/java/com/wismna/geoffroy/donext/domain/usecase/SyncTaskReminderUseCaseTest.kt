package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class SyncTaskReminderUseCaseTest {

    private val zone = ZoneId.of("America/Montreal")
    private val today = LocalDate.of(2026, 3, 10)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)

    private val taskRepository = mockk<TaskRepository>()
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val useCase = SyncTaskReminderUseCase(taskRepository, reminderScheduler, clock)

    private val task = Task(
        id = 1L,
        name = "Call",
        description = null,
        priority = Priority.NORMAL,
        isDone = false,
        isDeleted = false,
        taskListId = 1L,
        dueDate = today,
        dueTime = LocalTime.of(14, 0)
    )

    @Test
    fun `schedules a reminder for an open task due in the future`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task

        useCase(1L)

        verify { reminderScheduler.scheduleTaskReminder(1L, today.atTime(14, 0).atZone(zone).toInstant()) }
    }

    @Test
    fun `fires a past due reminder right away when it was never shown`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task.copy(dueTime = LocalTime.of(9, 0))

        useCase(1L)

        verify { reminderScheduler.scheduleTaskReminder(1L, clock.instant()) }
        verify(exactly = 0) { reminderScheduler.cancelTaskReminder(any()) }
    }

    @Test
    fun `cancels the alarm without dismissing once the reminder was shown`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task.copy(dueTime = LocalTime.of(9, 0), isReminderShown = true)

        useCase(1L)

        verify { reminderScheduler.cancelTaskReminder(1L) }
        verify(exactly = 0) { reminderScheduler.dismissTaskReminder(any()) }
        verify(exactly = 0) { reminderScheduler.scheduleTaskReminder(any(), any()) }
    }

    @Test
    fun `cancels the alarm when the task has no due time`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task.copy(dueTime = null)

        useCase(1L)

        verify { reminderScheduler.cancelTaskReminder(1L) }
        verify(exactly = 0) { reminderScheduler.dismissTaskReminder(any()) }
    }

    @Test
    fun `cancels and dismisses the reminder when the task is done`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task.copy(isDone = true)

        useCase(1L)

        verify { reminderScheduler.cancelTaskReminder(1L) }
        verify { reminderScheduler.dismissTaskReminder(1L) }
    }

    @Test
    fun `cancels and dismisses the reminder when the task is deleted`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task.copy(isDeleted = true)

        useCase(1L)

        verify { reminderScheduler.cancelTaskReminder(1L) }
        verify { reminderScheduler.dismissTaskReminder(1L) }
    }

    @Test
    fun `cancels and dismisses the reminder when the task no longer exists`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns null

        useCase(1L)

        verify { reminderScheduler.cancelTaskReminder(1L) }
        verify { reminderScheduler.dismissTaskReminder(1L) }
    }
}
