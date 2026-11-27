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
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private lateinit var createTaskUseCase: AddTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var uiEventBus: UiEventBus
    private lateinit var stickyEventsFlow: MutableSharedFlow<UiEvent>

    private lateinit var viewModel: TaskViewModel

    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2025, 9, 1)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)

    private fun pickerMillis(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

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
            uiEventBus,
            clock
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
            dueDate = LocalDate.of(2025, 10, 1),
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
    fun `onDueDateChanged keeps the picked calendar day whatever the device time zone`() {
        val pickerSelection = Instant.parse("2025-10-01T00:00:00Z").toEpochMilli()

        viewModel.onDueDateChanged(pickerSelection)

        assertThat(viewModel.dueDate).isEqualTo(LocalDate.of(2025, 10, 1))
    }

    @Test
    fun `save with blank title does nothing`() = runTest {
        stickyEventsFlow.emit(UiEvent.CreateNewTask(1L))
        advanceUntilIdle()
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 0) { createTaskUseCase(any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateTaskUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `save before taskListId is set does nothing`() = runTest {
        viewModel.onTitleChanged("Task")
        viewModel.save()
        advanceUntilIdle()

        coVerify(exactly = 0) { createTaskUseCase(any(), any(), any(), any(), any(), any()) }
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
                viewModel.dueDate,
                null
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
                null
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

    @Test
    fun `onDueTimeChanged updates due time`() {
        viewModel.onDueTimeChanged(LocalTime.of(14, 30))
        assertThat(viewModel.dueTime).isEqualTo(LocalTime.of(14, 30))
    }

    @Test
    fun `clearing the due date also clears the due time`() {
        viewModel.onDueDateChanged(Instant.parse("2025-10-01T12:00:00Z").toEpochMilli())
        viewModel.onDueTimeChanged(LocalTime.of(14, 30))

        viewModel.onDueDateChanged(null)

        assertThat(viewModel.dueDate).isNull()
        assertThat(viewModel.dueTime).isNull()
    }

    @Test
    fun `EditTask event populates due time`() = runTest {
        val task = Task(
            id = 7L, taskListId = 9L, name = "Call", description = "", priority = Priority.NORMAL,
            dueDate = LocalDate.of(2025, 10, 1), dueTime = LocalTime.of(9, 15),
            isDone = false, isDeleted = false
        )

        stickyEventsFlow.emit(UiEvent.EditTask(task))
        advanceUntilIdle()

        assertThat(viewModel.dueTime).isEqualTo(LocalTime.of(9, 15))
    }

    @Test
    fun `CreateNewTask event resets due time`() = runTest {
        viewModel.onDueTimeChanged(LocalTime.of(9, 15))

        stickyEventsFlow.emit(UiEvent.CreateNewTask(1L))
        advanceUntilIdle()

        assertThat(viewModel.dueTime).isNull()
    }

    @Test
    fun `save passes the due time to the create use case`() = runTest {
        stickyEventsFlow.emit(UiEvent.CreateNewTask(3L))
        advanceUntilIdle()
        viewModel.onTitleChanged("Call")
        viewModel.onDueDateChanged(Instant.parse("2025-10-01T12:00:00Z").toEpochMilli())
        viewModel.onDueTimeChanged(LocalTime.of(16, 0))

        viewModel.save()
        advanceUntilIdle()

        coVerify { createTaskUseCase(3L, "Call", "", Priority.NORMAL, viewModel.dueDate, LocalTime.of(16, 0)) }
    }

    @Test
    fun `a past time cannot be picked for a task due today`() {
        viewModel.onDueDateChanged(pickerMillis(today))

        assertThat(viewModel.isDueTimeAllowed(LocalTime.of(9, 0))).isFalse()
        assertThat(viewModel.isDueTimeAllowed(LocalTime.of(11, 0))).isTrue()

        viewModel.onDueTimeChanged(LocalTime.of(9, 0))
        assertThat(viewModel.dueTime).isNull()
    }

    @Test
    fun `any time can be picked for a future day`() {
        viewModel.onDueDateChanged(pickerMillis(today.plusDays(1)))

        assertThat(viewModel.isDueTimeAllowed(LocalTime.of(6, 0))).isTrue()
    }

    @Test
    fun `moving a task to today clears a due time that already passed`() {
        viewModel.onDueDateChanged(pickerMillis(today.plusDays(1)))
        viewModel.onDueTimeChanged(LocalTime.of(8, 0))

        viewModel.onDueDateChanged(pickerMillis(today))

        assertThat(viewModel.dueTime).isNull()
    }

    @Test
    fun `suggests the next full hour today and 9 AM on other days`() {
        viewModel.onDueDateChanged(pickerMillis(today))
        assertThat(viewModel.suggestedDueTime()).isEqualTo(LocalTime.of(11, 0))

        viewModel.onDueDateChanged(pickerMillis(today.plusDays(2)))
        assertThat(viewModel.suggestedDueTime()).isEqualTo(LocalTime.of(9, 0))
    }

    @Test
    fun `the suggested time does not wrap past midnight late in the evening`() {
        val lateClock = Clock.fixed(today.atTime(23, 20).atZone(zone).toInstant(), zone)
        val lateViewModel = TaskViewModel(createTaskUseCase, updateTaskUseCase, uiEventBus, lateClock)
        lateViewModel.onDueDateChanged(pickerMillis(today))

        assertThat(lateViewModel.suggestedDueTime()).isEqualTo(LocalTime.of(23, 59))
    }

    @Test
    fun `CloseTask event clears the due date and the done and deleted flags`() = runTest {
        stickyEventsFlow.emit(
            UiEvent.EditTask(
                Task(
                    id = 1L, taskListId = 2L, name = "T", description = "", priority = Priority.HIGH,
                    isDone = true, isDeleted = true, dueDate = today.plusDays(3), dueTime = LocalTime.of(9, 0)
                )
            )
        )
        advanceUntilIdle()

        stickyEventsFlow.emit(UiEvent.CloseTask)
        advanceUntilIdle()

        assertThat(viewModel.dueDate).isNull()
        assertThat(viewModel.dueTime).isNull()
        assertThat(viewModel.isDone).isFalse()
        assertThat(viewModel.isDeleted).isFalse()
    }
}
