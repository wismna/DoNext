package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ResetTaskReminderUseCaseTest {

    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 3, 10)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)

    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val syncTaskReminder = mockk<SyncTaskReminderUseCase>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val useCase = ResetTaskReminderUseCase(taskRepository, reminderScheduler, syncTaskReminder, clock)

    private fun task(dueDate: LocalDate, dueTime: LocalTime, isReminderShown: Boolean) = Task(
        id = 1L, name = "Call", description = null, priority = Priority.NORMAL,
        isDone = false, isDeleted = false, taskListId = 1L,
        dueDate = dueDate, dueTime = dueTime, isReminderShown = isReminderShown
    )

    @Test
    fun `a reopened task whose reminder already passed is marked shown before syncing`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task(today.minusDays(1), LocalTime.of(9, 0), isReminderShown = false)

        useCase(1L)

        coVerifyOrder {
            taskRepository.setReminderShown(1L, true)
            syncTaskReminder(1L)
        }
    }

    @Test
    fun `a task rescheduled to the future is re-armed`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task(today.plusDays(1), LocalTime.of(9, 0), isReminderShown = true)

        useCase(1L)

        coVerifyOrder {
            taskRepository.setReminderShown(1L, false)
            syncTaskReminder(1L)
        }
    }

    @Test
    fun `dismisses the notification of the previous reminder`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns task(today.plusDays(1), LocalTime.of(9, 0), isReminderShown = true)

        useCase(1L)

        verify { reminderScheduler.dismissTaskReminder(1L) }
    }

    @Test
    fun `a missing task is only synced`() = runTest {
        coEvery { taskRepository.getTaskById(1L) } returns null

        useCase(1L)

        coVerify(exactly = 0) { taskRepository.setReminderShown(any(), any()) }
        coVerify { syncTaskReminder(1L) }
    }
}
