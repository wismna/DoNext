package com.wismna.geoffroy.donext.presentation.viewmodel

import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.GetDueTodayTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDoneUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DueTodayViewModelTest {

    private lateinit var getDueTodayTasksUseCase: GetDueTodayTasksUseCase
    private lateinit var toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase
    private lateinit var toggleTaskDoneUseCase: ToggleTaskDoneUseCase
    private lateinit var uiEventBus: UiEventBus
    private lateinit var viewModel: DueTodayViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val tasksFlow = MutableSharedFlow<List<Task>>(replay = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getDueTodayTasksUseCase = mockk()
        toggleTaskDeletedUseCase = mockk(relaxed = true)
        toggleTaskDoneUseCase = mockk(relaxed = true)
        uiEventBus = mockk(relaxed = true)

        coEvery { getDueTodayTasksUseCase.invoke() } returns tasksFlow

        viewModel = DueTodayViewModel(
            getDueTodayTasksUseCase,
            toggleTaskDeletedUseCase,
            toggleTaskDoneUseCase,
            uiEventBus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dueTodayTasks updates when flow emits`() = runTest {
        val taskList = listOf(Task(taskListId = 0, id = 1, name = "Test Task", description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false))
        tasksFlow.emit(taskList)
        advanceUntilIdle()

        assertEquals(taskList, viewModel.dueTodayTasks)
    }

    @Test
    fun `onTaskClicked sends EditTask event`() = runTest {
        val task = Task(taskListId = 0, id = 42, name = "Click me", description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)

        viewModel.onTaskClicked(task)
        advanceUntilIdle()

        coVerify { uiEventBus.send(UiEvent.EditTask(task)) }
    }

    @Test
    fun `updateTaskDone toggles done and sends snackbar with undo`() = runTest {
        val taskId = 5L

        viewModel.updateTaskDone(taskId)
        advanceUntilIdle()

        coVerify { toggleTaskDoneUseCase(taskId, true) }
        coVerify {
            uiEventBus.send(
                match {
                    it is UiEvent.ShowUndoSnackbar &&
                    it.message == R.string.snackbar_message_task_done
                }
            )
        }
    }

    @Test
    fun `deleteTask toggles deleted and sends snackbar with undo`() = runTest {
        val taskId = 7L

        viewModel.deleteTask(taskId)
        advanceUntilIdle()

        coVerify { toggleTaskDeletedUseCase(taskId, true) }
        coVerify {
            uiEventBus.send(
                match{
                    it is UiEvent.ShowUndoSnackbar &&
                    it.message == R.string.snackbar_message_task_recycle
                }
            )
        }
    }
}
