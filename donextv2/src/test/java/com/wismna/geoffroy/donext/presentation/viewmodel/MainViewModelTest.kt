package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.navigation.NavBackStackEntry
import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val uiEventBus: UiEventBus = mockk(relaxUnitFun = true)
    private lateinit var getTaskListsFlow: MutableSharedFlow<List<TaskList>>
    private lateinit var getTaskListsUseCase: GetTaskListsUseCase
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        getTaskListsFlow = MutableSharedFlow()
        getTaskListsUseCase = mockk {
            every { this@mockk.invoke() } returns getTaskListsFlow
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initially isLoading is true and destinations are empty`() = runTest {
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)
        assertThat(viewModel.isLoading).isTrue()
        assertThat(viewModel.destinations).isEmpty()
    }

    @Test
    fun `when task lists are emitted they populate destinations and isLoading becomes false`() = runTest {
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)
        advanceUntilIdle()

        val lists = listOf(
            TaskList(id = 1L, name = "Work", isDeleted = false, order = 0),
            TaskList(id = 2L, name = "Personal", isDeleted = false, order = 1)
        )

        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        val expectedTaskDestinations = lists.map {
            AppDestination.TaskList(it.id!!, it.name)
        }

        assertThat(viewModel.destinations).containsAtLeastElementsIn(expectedTaskDestinations)
        assertThat(viewModel.destinations).containsAtLeast(
            AppDestination.ManageLists,
            AppDestination.RecycleBin,
            AppDestination.DueTodayList
        )
        assertThat(viewModel.isLoading).isFalse()
    }

    @Test
    fun `navigateBack sends UiEvent_NavigateBack`() = runTest {
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)

        viewModel.navigateBack()
        advanceUntilIdle()

        coVerify { uiEventBus.send(UiEvent.NavigateBack) }
    }

    @Test
    fun `onNewTaskButtonClicked sets showTaskSheet true and sends CreateNewTask`() = runTest {
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)
        val taskListId = 42L

        viewModel.onNewTaskButtonClicked(taskListId)
        advanceUntilIdle()

        assertThat(viewModel.showTaskSheet).isTrue()
        coVerify { uiEventBus.send(UiEvent.CreateNewTask(taskListId)) }
    }

    @Test
    fun `onDismissTaskSheet sets showTaskSheet false and clears sticky`() = runTest {
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)

        viewModel.showTaskSheet = true
        viewModel.onDismissTaskSheet()
        advanceUntilIdle()

        assertThat(viewModel.showTaskSheet).isFalse()
        coVerify { uiEventBus.send(UiEvent.CloseTask) }
        coVerify { uiEventBus.clearSticky() }
    }

    @Test
    fun `doesListExist returns true when taskListId is present`() = runTest {
        val lists = listOf(TaskList(id = 1L, name = "Work", isDeleted = false, order = 0))
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)
        advanceUntilIdle()
        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        assertThat(viewModel.doesListExist(1L)).isTrue()
        assertThat(viewModel.doesListExist(99L)).isFalse()
    }

    @Test
    fun `setCurrentDestination sets currentDestination based on navBackStackEntry`() = runTest {
        val lists = listOf(TaskList(id = 1L, name = "Work", isDeleted = false, order = 0))
        viewModel = MainViewModel(getTaskListsUseCase, uiEventBus)
        advanceUntilIdle()
        getTaskListsFlow.emit(lists)
        advanceUntilIdle()

        val entry = mockk<NavBackStackEntry> {
            every { destination.route } returns AppDestination.TaskList(1L, "Work").route
            every { arguments?.getLong("taskListId") } returns 1L
        }

        viewModel.setCurrentDestination(entry)
        assertThat(viewModel.currentDestination).isEqualTo(AppDestination.TaskList(1L, "Work"))
    }
}
