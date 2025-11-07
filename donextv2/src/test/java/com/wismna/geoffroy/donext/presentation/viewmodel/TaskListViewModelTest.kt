package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.GetTasksForListUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDoneUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.Priority
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private lateinit var getTasksForListUseCase: GetTasksForListUseCase
    private lateinit var toggleTaskDoneUseCase: ToggleTaskDoneUseCase
    private lateinit var toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase
    private lateinit var uiEventBus: UiEventBus
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var getTasksFlow: MutableSharedFlow<List<Task>>
    private lateinit var viewModel: TaskListViewModel

    private val testTaskListId = 100L

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())

        getTasksForListUseCase = mockk()
        toggleTaskDoneUseCase = mockk(relaxed = true)
        toggleTaskDeletedUseCase = mockk(relaxed = true)
        uiEventBus = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("taskListId" to testTaskListId))

        getTasksFlow = MutableSharedFlow()
        every { getTasksForListUseCase(testTaskListId) } returns getTasksFlow

        viewModel = TaskListViewModel(
            savedStateHandle,
            getTasksForListUseCase,
            toggleTaskDoneUseCase,
            toggleTaskDeletedUseCase,
            uiEventBus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- TESTS ---

    @Test
    fun `initial state is loading and tasks empty`() = runTest {
        assertThat(viewModel.isLoading).isTrue()
        assertThat(viewModel.tasks).isEmpty()
    }

    @Test
    fun `emitting tasks updates list and stops loading`() = runTest {
        val tasks = listOf(
            Task(id = 1L, name = "Write docs", taskListId = testTaskListId, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false),
            Task(id = 2L, name = "Code review", taskListId = testTaskListId, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)
        )

        advanceUntilIdle()
        getTasksFlow.emit(tasks)
        advanceUntilIdle()

        assertThat(viewModel.isLoading).isFalse()
        assertThat(viewModel.tasks).isEqualTo(tasks)
    }

    @Test
    fun `onTaskClicked sends EditTask event`() = runTest {
        val task = Task(id = 1L, name = "Test task", taskListId = testTaskListId, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)

        viewModel.onTaskClicked(task)
        advanceUntilIdle()

        coVerify { uiEventBus.send(UiEvent.EditTask(task)) }
    }

    @Test
    fun `updateTaskDone marks task done and sends snackbar with undo`() = runTest {
        val taskId = 3L

        viewModel.updateTaskDone(taskId, true)
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
    fun `updateTaskDone undoAction marks task undone`() = runTest {
        val taskId = 7L
        val eventSlot = slot<UiEvent>()

        coEvery { uiEventBus.send(capture(eventSlot)) } just Runs

        viewModel.updateTaskDone(taskId, true)
        advanceUntilIdle()

        val snackbar = eventSlot.captured as UiEvent.ShowUndoSnackbar
        snackbar.undoAction.invoke()
        advanceUntilIdle()

        coVerify { toggleTaskDoneUseCase(taskId, false) }
    }

    @Test
    fun `deleteTask marks task deleted and sends snackbar`() = runTest {
        val taskId = 9L

        viewModel.deleteTask(taskId)
        advanceUntilIdle()

        coVerify { toggleTaskDeletedUseCase(taskId, true) }
        coVerify {
            uiEventBus.send(
                match {
                    it is UiEvent.ShowUndoSnackbar &&
                            it.message == R.string.snackbar_message_task_recycle
                }
            )
        }
    }

    @Test
    fun `deleteTask undoAction restores task`() = runTest {
        val taskId = 10L
        val eventSlot = slot<UiEvent>()

        coEvery { uiEventBus.send(capture(eventSlot)) } just Runs

        viewModel.deleteTask(taskId)
        advanceUntilIdle()

        val snackbar = eventSlot.captured as UiEvent.ShowUndoSnackbar
        snackbar.undoAction.invoke()
        advanceUntilIdle()

        coVerify { toggleTaskDeletedUseCase(taskId, false) }
    }
}
