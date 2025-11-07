package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import com.wismna.geoffroy.donext.domain.usecase.EmptyRecycleBinUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetDeletedTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.PermanentlyDeleteTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import com.wismna.geoffroy.donext.R
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
class RecycleBinViewModelTest {

    private lateinit var getDeletedTasksUseCase: GetDeletedTasksUseCase
    private lateinit var toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase
    private lateinit var permanentlyDeleteTaskUseCase: PermanentlyDeleteTaskUseCase
    private lateinit var emptyRecycleBinUseCase: EmptyRecycleBinUseCase
    private lateinit var uiEventBus: UiEventBus
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var getDeletedTasksFlow: MutableSharedFlow<List<TaskWithListName>>
    private lateinit var viewModel: RecycleBinViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())

        getDeletedTasksUseCase = mockk()
        toggleTaskDeletedUseCase = mockk(relaxed = true)
        permanentlyDeleteTaskUseCase = mockk(relaxed = true)
        emptyRecycleBinUseCase = mockk(relaxed = true)
        uiEventBus = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()

        getDeletedTasksFlow = MutableSharedFlow()
        every { getDeletedTasksUseCase() } returns getDeletedTasksFlow

        viewModel = RecycleBinViewModel(
            getDeletedTasksUseCase,
            toggleTaskDeletedUseCase,
            permanentlyDeleteTaskUseCase,
            emptyRecycleBinUseCase,
            uiEventBus,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- TESTS ---

    @Test
    fun `initial state is empty`() = runTest {
        assertThat(viewModel.deletedTasks).isEmpty()
        assertThat(viewModel.taskToDeleteFlow.value).isNull()
        assertThat(viewModel.emptyRecycleBinFlow.value).isFalse()
    }

    @Test
    fun `emitting deleted tasks updates deletedTasks list`() = runTest {
        val tasks = listOf(
            TaskWithListName(Task(id = 1L, name = "Old task", taskListId = 0L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false), listName = "Work"),
            TaskWithListName(Task(id = 2L, name = "Done task", taskListId = 0L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false), listName = "Home")
        )

        advanceUntilIdle()
        getDeletedTasksFlow.emit(tasks)
        advanceUntilIdle()

        assertThat(viewModel.deletedTasks).isEqualTo(tasks)
    }

    @Test
    fun `restore toggles deletion and shows undo snackbar`() = runTest {
        val taskId = 5L

        viewModel.restore(taskId)
        advanceUntilIdle()

        coVerify { toggleTaskDeletedUseCase(taskId, false) }
        coVerify {
            uiEventBus.send(
                match {
                    it is UiEvent.ShowUndoSnackbar &&
                    it.message == R.string.snackbar_message_task_restore
                }
            )
        }
    }

    @Test
    fun `onTaskClicked sends EditTask UiEvent`() = runTest {
        val task = Task(id = 1L, name = "T", taskListId = 1L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)

        viewModel.onTaskClicked(task)
        advanceUntilIdle()

        coVerify { uiEventBus.send(UiEvent.EditTask(task)) }
    }

    @Test
    fun `onEmptyRecycleBinRequest sets flag to true`() = runTest {
        viewModel.onEmptyRecycleBinRequest()
        assertThat(viewModel.emptyRecycleBinFlow.value).isTrue()
    }

    @Test
    fun `onCancelEmptyRecycleBinRequest sets flag to false`() = runTest {
        savedStateHandle["emptyRecycleBin"] = true
        viewModel.onCancelEmptyRecycleBinRequest()
        assertThat(viewModel.emptyRecycleBinFlow.value).isFalse()
    }

    @Test
    fun `emptyRecycleBin calls use case and resets flag`() = runTest {
        savedStateHandle["emptyRecycleBin"] = true

        viewModel.emptyRecycleBin()
        advanceUntilIdle()

        coVerify { emptyRecycleBinUseCase() }
        assertThat(viewModel.emptyRecycleBinFlow.value).isFalse()
    }

    @Test
    fun `onTaskDeleteRequest sets taskToDelete id`() = runTest {
        viewModel.onTaskDeleteRequest(42L)
        assertThat(viewModel.taskToDeleteFlow.value).isEqualTo(42L)
    }

    @Test
    fun `onConfirmDelete calls use case and clears task id`() = runTest {
        savedStateHandle["taskToDeleteId"] = 7L

        viewModel.onConfirmDelete()
        advanceUntilIdle()

        coVerify { permanentlyDeleteTaskUseCase(7L) }
        assertThat(viewModel.taskToDeleteFlow.value).isNull()
    }

    @Test
    fun `onCancelDelete clears task id`() = runTest {
        savedStateHandle["taskToDeleteId"] = 10L

        viewModel.onCancelDelete()
        assertThat(viewModel.taskToDeleteFlow.value).isNull()
    }

    @Test
    fun `simultaneous flow emissions update deleted tasks and flags independently`() = runTest {
        val tasks = listOf(TaskWithListName(Task(id = 1L, name = "Trash", taskListId = 0L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false), listName = "Work"))
        advanceUntilIdle()

        // Emit tasks while also updating the recycle bin flag
        getDeletedTasksFlow.emit(tasks)
        savedStateHandle["emptyRecycleBin"] = true
        advanceUntilIdle()

        assertThat(viewModel.deletedTasks).isEqualTo(tasks)
        assertThat(viewModel.emptyRecycleBinFlow.value).isTrue()
    }

    @Test
    fun `restore snackbar undoAction re-deletes the task`() = runTest {
        val taskId = 99L
        val eventSlot = slot<UiEvent>()

        // Intercept UiEvent.ShowUndoSnackbar to get the undoAction
        coEvery { uiEventBus.send(capture(eventSlot)) } just Runs

        viewModel.restore(taskId)
        advanceUntilIdle()

        // Ensure the event is a ShowUndoSnackbar
        val snackbarEvent = eventSlot.captured as UiEvent.ShowUndoSnackbar

        // Run the undo lambda
        snackbarEvent.undoAction.invoke()
        advanceUntilIdle()

        // Verify that it re-deletes the task (sets deleted = true again)
        coVerify { toggleTaskDeletedUseCase(taskId, true) }
    }

}
