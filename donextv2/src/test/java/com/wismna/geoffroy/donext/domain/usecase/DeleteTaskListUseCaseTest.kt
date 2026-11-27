package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteTaskListUseCaseTest {

    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val taskListRepository = mockk<TaskListRepository>(relaxed = true)
    private val syncTaskReminder = mockk<SyncTaskReminderUseCase>(relaxed = true)
    private val resetTaskReminder = mockk<ResetTaskReminderUseCase>(relaxed = true)
    private val useCase = DeleteTaskListUseCase(taskRepository, taskListRepository, syncTaskReminder, resetTaskReminder)

    @Test
    fun `deleting a list also toggles all its tasks, list last`() = runTest {
        useCase(5L, true)

        coVerifyOrder {
            taskRepository.toggleAllTasksInListDeleted(5L, true)
            taskListRepository.deleteTaskList(5L, true)
        }
    }

    @Test
    fun `restoring a list also restores all its tasks`() = runTest {
        useCase(5L, false)

        coVerify { taskRepository.toggleAllTasksInListDeleted(5L, false) }
        coVerify { taskListRepository.deleteTaskList(5L, false) }
    }

    @Test
    fun `deleting a list syncs the reminder of every task in it`() = runTest {
        coEvery { taskRepository.getTaskIdsForList(5L) } returns listOf(1L, 2L)

        useCase(5L, true)

        coVerify { syncTaskReminder(1L) }
        coVerify { syncTaskReminder(2L) }
    }

    @Test
    fun `restoring a list resets the reminder of every task in it`() = runTest {
        coEvery { taskRepository.getTaskIdsForList(5L) } returns listOf(1L, 2L)

        useCase(5L, false)

        coVerify { resetTaskReminder(1L) }
        coVerify { resetTaskReminder(2L) }
        coVerify(exactly = 0) { syncTaskReminder(any()) }
    }
}
