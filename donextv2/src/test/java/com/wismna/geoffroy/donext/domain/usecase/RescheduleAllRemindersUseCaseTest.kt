package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RescheduleAllRemindersUseCaseTest {

    private val taskRepository = mockk<TaskRepository>()
    private val syncTaskReminder = mockk<SyncTaskReminderUseCase>(relaxed = true)
    private val scheduleDailyDigest = mockk<ScheduleDailyDigestUseCase>(relaxed = true)
    private val useCase = RescheduleAllRemindersUseCase(taskRepository, syncTaskReminder, scheduleDailyDigest)

    private fun task(id: Long) = Task(
        id = id,
        name = "Task $id",
        description = null,
        priority = Priority.NORMAL,
        isDone = false,
        isDeleted = false,
        taskListId = 1L
    )

    @Test
    fun `syncs every open timed task and the daily digest`() = runTest {
        coEvery { taskRepository.getOpenTasksWithDueTime() } returns listOf(task(1L), task(2L))

        useCase()

        coVerify { syncTaskReminder(1L) }
        coVerify { syncTaskReminder(2L) }
        coVerify(exactly = 1) { scheduleDailyDigest() }
    }

    @Test
    fun `still schedules the digest when no task has a due time`() = runTest {
        coEvery { taskRepository.getOpenTasksWithDueTime() } returns emptyList()

        useCase()

        coVerify(exactly = 0) { syncTaskReminder(any()) }
        coVerify(exactly = 1) { scheduleDailyDigest() }
    }
}
