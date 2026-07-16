package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleTaskDeletedUseCaseTest {

    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val taskListRepository = mockk<TaskListRepository>(relaxed = true)
    private val useCase = ToggleTaskDeletedUseCase(taskRepository, taskListRepository)

    private val task = Task(
        id = 1L,
        taskListId = 5L,
        name = "Task",
        description = "",
        priority = Priority.NORMAL,
        isDone = false,
        isDeleted = true
    )

    @Test
    fun `restoring a task also restores its soft-deleted parent list`() = runTest {
        val deletedList = TaskList(id = 5L, name = "List", isDeleted = true, order = 0)
        coEvery { taskRepository.getTaskById(1L) } returns task
        coEvery { taskListRepository.getTaskListById(5L) } returns deletedList

        useCase(1L, false)

        coVerify { taskListRepository.updateTaskList(deletedList.copy(isDeleted = false)) }
        coVerify { taskRepository.toggleTaskDeleted(1L, false) }
    }

    @Test
    fun `restoring a task whose parent list is not deleted leaves the list untouched`() = runTest {
        val activeList = TaskList(id = 5L, name = "List", isDeleted = false, order = 0)
        coEvery { taskRepository.getTaskById(1L) } returns task
        coEvery { taskListRepository.getTaskListById(5L) } returns activeList

        useCase(1L, false)

        coVerify(exactly = 0) { taskListRepository.updateTaskList(any()) }
        coVerify { taskRepository.toggleTaskDeleted(1L, false) }
    }

    @Test
    fun `soft-deleting a task never touches its parent list`() = runTest {
        useCase(1L, true)

        coVerify(exactly = 0) { taskRepository.getTaskById(any()) }
        coVerify(exactly = 0) { taskListRepository.getTaskListById(any()) }
        coVerify { taskRepository.toggleTaskDeleted(1L, true) }
    }
}
