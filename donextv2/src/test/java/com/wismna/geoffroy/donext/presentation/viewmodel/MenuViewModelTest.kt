package com.wismna.geoffroy.donext.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.usecase.GetDueTodayTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsWithOverdueUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class MenuViewModelTest {

    private lateinit var getTaskListsWithOverdueUseCase: GetTaskListsWithOverdueUseCase
    private lateinit var getDueTodayTasksUseCase: GetDueTodayTasksUseCase
    private lateinit var uiEventBus: UiEventBus

    private lateinit var taskListsFlow: MutableSharedFlow<List<TaskListWithOverdue>>
    private lateinit var dueTodayTasksFlow: MutableSharedFlow<List<Task>>
    private lateinit var viewModel: MenuViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())

        getTaskListsWithOverdueUseCase = mockk()
        getDueTodayTasksUseCase = mockk()
        uiEventBus = mockk(relaxed = true)

        taskListsFlow = MutableSharedFlow()
        dueTodayTasksFlow = MutableSharedFlow()

        every { getTaskListsWithOverdueUseCase() } returns taskListsFlow
        every { getDueTodayTasksUseCase() } returns dueTodayTasksFlow

        viewModel = MenuViewModel(
            getTaskListsWithOverdueUseCase,
            getDueTodayTasksUseCase,
            uiEventBus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- TESTS ---

    @Test
    fun `initially has empty lists and zero due today`() = runTest {
        assertThat(viewModel.taskLists).isEmpty()
        assertThat(viewModel.dueTodayTasksCount).isEqualTo(0)
    }

    @Test
    fun `emitting task lists updates taskLists`() = runTest {
        val lists = listOf(
            TaskListWithOverdue(id = 1L, name = "Work", overdueCount = 2),
            TaskListWithOverdue(id = 2L, name = "Home", overdueCount = 0)
        )

        advanceUntilIdle()
        taskListsFlow.emit(lists)
        advanceUntilIdle()

        assertThat(viewModel.taskLists).isEqualTo(lists)
    }

    @Test
    fun `emitting due today tasks updates count`() = runTest {
        val tasks = listOf(
            Task(id = 1L, name = "Task A", taskListId = 1L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false),
            Task(id = 2L, name = "Task B", taskListId = 1L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)
        )

        advanceUntilIdle()
        dueTodayTasksFlow.emit(tasks)
        advanceUntilIdle()

        assertThat(viewModel.dueTodayTasksCount).isEqualTo(2)
    }

    @Test
    fun `navigateTo sends UiEvent when route is different`() = runTest {
        val route = "tasks"
        val currentRoute = "home"

        viewModel.navigateTo(route, currentRoute)
        advanceUntilIdle()

        coVerify {
            uiEventBus.send(
                match {
                    it is UiEvent.Navigate && it.route == route
                }
            )
        }
    }

    @Test
    fun `navigateTo does nothing when route is the same`() = runTest {
        val route = "tasks"

        viewModel.navigateTo(route, route)
        advanceUntilIdle()

        coVerify(exactly = 0) { uiEventBus.send(any()) }
    }

    @Test
    fun `emitting both task lists and due today tasks updates both states`() = runTest {
        val lists = listOf(
            TaskListWithOverdue(id = 1L, name = "Work", overdueCount = 3),
            TaskListWithOverdue(id = 2L, name = "Personal", overdueCount = 1)
        )
        val tasks = listOf(
            Task(id = 10L, name = "Buy groceries", taskListId = 2L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false),
            Task(id = 11L, name = "Finish report", taskListId = 1L, description = "", priority = Priority.NORMAL, isDone = false, isDeleted = false)
        )

        // Let the ViewModel collectors start
        advanceUntilIdle()

        // Emit from both flows (simulating data updates happening nearly simultaneously)
        taskListsFlow.emit(lists)
        dueTodayTasksFlow.emit(tasks)
        advanceUntilIdle()

        // Verify both internal states are updated independently and correctly
        assertThat(viewModel.taskLists).isEqualTo(lists)
        assertThat(viewModel.dueTodayTasksCount).isEqualTo(2)
    }

}
