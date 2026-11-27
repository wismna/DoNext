package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReminderSettingsUseCasesTest {

    @Test
    fun `setting the digest time saves it and clears the pending catch-up before rescheduling`() = runTest {
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)
        val scheduleDailyDigest = mockk<ScheduleDailyDigestUseCase>(relaxed = true)

        SetDailyReminderTimeUseCase(settingsRepository, scheduleDailyDigest)(LocalTime.of(7, 45))

        coVerifyOrder {
            settingsRepository.setDailyReminderTime(LocalTime.of(7, 45))
            settingsRepository.setPendingDigestDate(null)
            scheduleDailyDigest()
        }
    }

    @Test
    fun `exact reminders availability comes from the scheduler`() {
        val reminderScheduler = mockk<ReminderScheduler>()
        every { reminderScheduler.canScheduleExactReminders() } returns false

        assertThat(CanScheduleExactRemindersUseCase(reminderScheduler)()).isFalse()
    }

    @Test
    fun `consuming a reminder shows a pending one and marks it shown only once displayed`() = runTest {
        val zone = ZoneId.of("UTC")
        val day = LocalDate.of(2026, 3, 10)
        val clock = Clock.fixed(day.atStartOfDay(zone).toInstant(), zone)
        val taskRepository = mockk<TaskRepository>(relaxed = true)
        val task = Task(
            id = 1L, name = "Call", description = null, priority = Priority.NORMAL,
            isDone = false, isDeleted = false, taskListId = 1L,
            dueDate = day, dueTime = LocalTime.of(10, 0)
        )
        val useCase = ConsumeTaskReminderUseCase(taskRepository, clock)

        val shown = mutableListOf<Task>()
        coEvery { taskRepository.getTaskById(1L) } returns task
        useCase(1L) { shown += it; true }
        assertThat(shown).containsExactly(task)
        coVerify(exactly = 1) { taskRepository.setReminderShown(1L, true) }

        coEvery { taskRepository.getTaskById(1L) } returns task.copy(isReminderShown = true)
        useCase(1L) { shown += it; true }

        coEvery { taskRepository.getTaskById(1L) } returns task.copy(isDone = true)
        useCase(1L) { shown += it; true }

        assertThat(shown).hasSize(1)
        coVerify(exactly = 1) { taskRepository.setReminderShown(1L, true) }
    }

    @Test
    fun `a reminder that could not be displayed stays pending`() = runTest {
        val zone = ZoneId.of("UTC")
        val day = LocalDate.of(2026, 3, 10)
        val clock = Clock.fixed(day.atStartOfDay(zone).toInstant(), zone)
        val taskRepository = mockk<TaskRepository>(relaxed = true)
        coEvery { taskRepository.getTaskById(1L) } returns Task(
            id = 1L, name = "Call", description = null, priority = Priority.NORMAL,
            isDone = false, isDeleted = false, taskListId = 1L,
            dueDate = day, dueTime = LocalTime.of(10, 0)
        )

        ConsumeTaskReminderUseCase(taskRepository, clock)(1L) { false }

        coVerify(exactly = 0) { taskRepository.setReminderShown(any(), any()) }
    }

    @Test
    fun `marking the digest shown clears its pending date`() = runTest {
        val settingsRepository = mockk<SettingsRepository>(relaxed = true)

        MarkDailyDigestShownUseCase(settingsRepository)()

        coVerify { settingsRepository.setPendingDigestDate(null) }
    }
}
