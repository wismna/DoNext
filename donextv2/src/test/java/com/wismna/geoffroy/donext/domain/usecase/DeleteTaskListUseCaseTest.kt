package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteTaskListUseCaseTest {

    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val taskListRepository = mockk<TaskListRepository>(relaxed = true)
    private val useCase = DeleteTaskListUseCase(taskRepository, taskListRepository)

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
}
