package com.wismna.geoffroy.donext.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.AddTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskUseCase
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private lateinit var createTaskUseCase: AddTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var uiEventBus: UiEventBus
    private lateinit var stickyEventsFlow: MutableSharedFlow<UiEvent>

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())

        createTaskUseCase = mockk(relaxed = true)
        updateTaskUseCase = mockk(relaxed = true)
        uiEventBus = mockk(relaxed = true)

        stickyEventsFlow = MutableSharedFlow()
        every { uiEventBus.stickyEvents } returns stickyEventsFlow

        viewModel = TaskViewModel(
            createTaskUseCase,
            updateTaskUseCase,
            uiEventBus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- TESTS ---

    @Test
    fun `initial state is blank and not editing`() = runTest {
        assertThat(viewModel.title).isEmpty()
        assertThat(viewModel.description).isEmpty()
        assertThat(viewModel.priority).isEqualTo(Priority.NORMAL)
        assertThat(viewModel.dueDate).isNull()
        assertThat(viewModel.isDone).isFalse()
        assertThat(viewModel.isDeleted).isFalse()
        assertThat(viewModel.taskListId).isNull()
        assertThat(viewModel.isEditing()).isFalse()
    }

    @Test
    fun `CreateNewTask event resets fields and sets taskListId`() = runTest {
        stickyEventsFlow.emit(UiEvent.CreateNewTask(42L))
        advanceUntilIdle()

        assertThat(viewModel.isEditing()).isFalse()
        assertThat(viewModel.taskListId).isEqualTo(42L)
        assertThat(viewModel.title).isEmpty()
        assertThat(viewModel.description).isEmpty()
        assertThat(viewModel.priority).isEqualTo(Priority.NORMAL)
        assertThat(viewModel.dueDate).isNull()
        assertThat(viewModel.isDeleted).isFalse()
    }

    @Test
    fun `EditTask event populates fields from existing task`() = runTest {
        val task = Task(
            id = 7L,
            taskListId = 9L,
            name = "Fix bug",
            description = "Null pointer issue",
            priority = Priority.HIGH,
            dueDate = Instant.parse("2025-10-01T12:00:00Z").toEpochMilli(),
            isDone = true,
            isDeleted = false
        )

        stickyEventsFlow.emit(UiEvent.EditTask(task))
        advanceUntilIdle()

        assertThat(viewModel.isEditing()).isTrue()
        assertThat(viewModel.editingTaskId).isEqualTo(7L)
        assertThat(viewModel.taskListId).isEqualTo(9L)
        assertThat(viewModel.title).isEqualTo("Fix bug")
        assertThat(viewModel.description).isEqualTo("Null pointer issue")
        assertThat(viewModel.priority).isEqualTo(Priority.HIGH)
        assertThat(viewModel.dueDate).isEqualTo(task.dueDate)
        assertThat(viewModel.isDone).isTrue()
        assertThat(viewModel.isDeleted).isFalse()
    }

    @Test
    fun `CloseTask event resets state`() = runTest {
        // set up as editing
        stickyEventsFlow.emit(
            UiEvent.EditTask(
                Task(id = 1L, taskListId = 2L, name = "T", description = "D", priority = Priority.HIGH, isDone = false, isDeleted = false)
            )
        )
        advanceUntilIdle()

        stickyEventsFlow.emit(UiEvent.CloseTask)
        advanceUntilIdle()

        assertThat(viewModel.title).isEmpty()
        assertThat(viewModel.description).isEmpty()
        assertThat(viewModel.priority).isEqualTo(Priority.NORMAL)
        assertThat(viewModel.editingTaskId).isNull()
        assertThat(viewModel.taskListId).isNull()
    }

    @Test
    fun `onTitleChanged updates title`() {
        viewModel.onTitleChanged("New title")
        assertThat(viewModel.title).isEqualTo("New title")
    }

    @Test
    fun `onDescriptionChanged updates description`() {
        viewModel.onDescriptionChanged("Some description")
        assertThat(viewModel.description).isEqualTo("Some description")
    }

    @Test
    fun `onPriorityChanged updates priority`() {
        viewModel.onPriorityChanged(Priority.HIGH)
        assertThat(viewModel.priority).isEqualTo(Priority.HIGH)
    }

    @Test
    fun `onDueDateChanged normalizes date to start of day in system timezone`() {
        val utcMidday = Instant.parse("2025-10-01T12:00:00Z").toEpochMilli()
        viewModel.onDueDateChanged(utcMidday)

        val expectedStartOfDay =
            Instant.ofEpochMilli(utcMidday)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        assertThat(viewModel.dueDate).isEqualTo(expectedStartOfDay)
    }

    @Test
    fun `save with blank title does nothing`() = runTest {
        stickyEventsFlow.emit(UiEvent.CreateNewTask(1L))
        advanceUntilIdle()
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 0) { createTaskUseCase(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `save creates task when not editing`() = runTest {stickyEventsFlow.emit(UiEvent.CreateNewTask(3L))
        advanceUntilIdle()
        viewModel.onTitleChanged("New Task")
        viewModel.onDescriptionChanged("Description")
        viewModel.onPriorityChanged(Priority.HIGH)
        val due = Instant.parse("2025-10-01T12:00:00Z").toEpochMilli()
        viewModel.onDueDateChanged(due)

        viewModel.save()
        advanceUntilIdle()

        coVerify {
            createTaskUseCase(
                3L,
                "New Task",
                "Description",
                Priority.HIGH,
                viewModel.dueDate
            )
        }
    }

    @Test
    fun `save updates task when editing`() = runTest {
        val task = Task(
            id = 10L,
            taskListId = 5L,
            name = "Old Task",
            description = "Old desc",
            priority = Priority.NORMAL,
            dueDate = null,
            isDone = false,
            isDeleted = false
        )

        stickyEventsFlow.emit(UiEvent.EditTask(task))
        advanceUntilIdle()

        viewModel.onTitleChanged("Updated Task")
        viewModel.onDescriptionChanged("Updated desc")

        viewModel.save()
        advanceUntilIdle()

        coVerify {
            updateTaskUseCase(
                10L,
                5L,
                "Updated Task",
                "Updated desc",
                Priority.NORMAL,
                null,
                false
            )
        }
    }

    @Test
    fun `save calls onDone callback after save completes`() = runTest {
        var doneCalled = false

        stickyEventsFlow.emit(UiEvent.CreateNewTask(2L))
        advanceUntilIdle()

        viewModel.onTitleChanged("Task")
        viewModel.save { doneCalled = true }
        advanceUntilIdle()

        assertThat(doneCalled).isTrue()
    }
}