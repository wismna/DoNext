package com.wismna.geoffroy.donext.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.usecase.AddTaskListUseCase
import com.wismna.geoffroy.donext.domain.usecase.DeleteTaskListUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskListUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import com.wismna.geoffroy.donext.R
import io.mockk.*
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
class ManageListsViewModelTest {

    private lateinit var getTaskListsUseCase: GetTaskListsUseCase
    private lateinit var addTaskListUseCase: AddTaskListUseCase
    private lateinit var updateTaskListUseCase: UpdateTaskListUseCase
    private lateinit var deleteTaskListUseCase: DeleteTaskListUseCase
    private lateinit var uiEventBus: UiEventBus

    private lateinit var getTaskListsFlow: MutableSharedFlow<List<TaskList>>
    private lateinit var viewModel: ManageListsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())

        getTaskListsUseCase = mockk()
        addTaskListUseCase = mockk(relaxed = true)
        updateTaskListUseCase = mockk(relaxed = true)
        deleteTaskListUseCase = mockk(relaxed = true)
        uiEventBus = mockk(relaxed = true)

        getTaskListsFlow = MutableSharedFlow()
        every { getTaskListsUseCase() } returns getTaskListsFlow

        viewModel = ManageListsViewModel(
            getTaskListsUseCase,
            addTaskListUseCase,
            updateTaskListUseCase,
            deleteTaskListUseCase,
            uiEventBus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initially has empty task list`() = runTest {
        assertThat(viewModel.taskLists).isEmpty()
        assertThat(viewModel.taskCount).isEqualTo(0)
    }

    @Test
    fun `emitting lists updates taskLists and taskCount`() = runTest {
        val lists = listOf(
            TaskList(id = 1L, name = "Work", isDeleted = false, order = 0),
            TaskList(id = 2L, name = "Home", isDeleted = false, order = 1)
        )

        advanceUntilIdle()
        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        assertThat(viewModel.taskLists).isEqualTo(lists)
        assertThat(viewModel.taskCount).isEqualTo(2)
    }

    @Test
    fun `createTaskList calls use case`() = runTest {
        val title = "Groceries"
        val order = 1

        viewModel.createTaskList(title, order)
        advanceUntilIdle()

        coVerify { addTaskListUseCase(title, order) }
    }

    @Test
    fun `updateTaskListName calls use case`() = runTest {
        val taskList = TaskList(id = 1L, name = "Updated", isDeleted = false, order = 0)

        viewModel.updateTaskListName(taskList)
        advanceUntilIdle()

        coVerify { updateTaskListUseCase(1L, "Updated", 0) }
    }

    @Test
    fun `deleteTaskList calls use case and sends snackbar`() = runTest {
        val taskListId = 10L

        viewModel.deleteTaskList(taskListId)
        advanceUntilIdle()

        coVerify { deleteTaskListUseCase(taskListId, true) }
        coVerify {
            uiEventBus.send(
                match {
                    it is UiEvent.ShowUndoSnackbar &&
                    it.message == R.string.snackbar_message_task_list_recycle
                }
            )
        }
    }

    @Test
    fun `moveTaskList reorders the task list correctly`() = runTest {
        val lists = listOf(
            TaskList(id = 1L, name = "A", isDeleted = false, order = 0),
            TaskList(id = 2L, name = "B", isDeleted = false, order = 1),
            TaskList(id = 3L, name = "C", isDeleted = false, order = 2)
        )

        advanceUntilIdle()
        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        viewModel.moveTaskList(fromIndex = 0, toIndex = 2)
        assertThat(viewModel.taskLists.map { it.id }).isEqualTo(listOf(2L, 3L, 1L))
    }

    @Test
    fun `commitTaskListOrder updates only reordered lists`() = runTest {
        val lists = listOf(
            TaskList(id = 1L, name = "A", isDeleted = false, order = 0),
            TaskList(id = 2L, name = "B", isDeleted = false, order = 1),
            TaskList(id = 3L, name = "C", isDeleted = false, order = 2)
        )

        advanceUntilIdle()
        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        // Simulate reordering
        viewModel.moveTaskList(fromIndex = 2, toIndex = 0)
        viewModel.commitTaskListOrder()
        advanceUntilIdle()

        coVerify { updateTaskListUseCase(3L, "C", 0) }
        coVerify { updateTaskListUseCase(1L, "A", 1) }
        coVerify { updateTaskListUseCase(2L, "B", 2) }
    }
}
