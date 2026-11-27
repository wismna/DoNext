package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TaskMutationUseCasesTest {

    private val now = Instant.parse("2026-03-10T15:00:00Z")
    private val clock = Clock.fixed(now, ZoneId.of("UTC"))
    private val repository = mockk<TaskRepository>(relaxed = true)
    private val syncTaskReminder = mockk<SyncTaskReminderUseCase>(relaxed = true)
    private val resetTaskReminder = mockk<ResetTaskReminderUseCase>(relaxed = true)
    private val updateTask = UpdateTaskUseCase(repository, syncTaskReminder, resetTaskReminder)
    private val toggleTaskDone = ToggleTaskDoneUseCase(repository, syncTaskReminder, resetTaskReminder, clock)

    private val existingTask = Task(
        id = 4L,
        name = "Old",
        description = "Old desc",
        priority = Priority.LOW,
        isDone = false,
        isDeleted = false,
        taskListId = 1L,
        dueDate = LocalDate.of(2026, 3, 1),
        dueTime = LocalTime.of(8, 0),
        createdAt = 123L,
        completedAt = null
    )

    @Test
    fun `adding a task stamps its creation time and syncs its reminder`() = runTest {
        val inserted = slot<Task>()
        coEvery { repository.insertTask(capture(inserted)) } returns 42L

        AddTaskUseCase(repository, syncTaskReminder, clock)(1L, "Call", null, Priority.HIGH, LocalDate.of(2026, 3, 20), LocalTime.of(9, 30))

        assertThat(inserted.captured.createdAt).isEqualTo(now.toEpochMilli())
        assertThat(inserted.captured.dueTime).isEqualTo(LocalTime.of(9, 30))
        assertThat(inserted.captured.completedAt).isNull()
        assertThat(inserted.captured.isReminderShown).isFalse()
        coVerify { syncTaskReminder(42L) }
    }

    @Test
    fun `adding a task without a due date drops the due time`() = runTest {
        val inserted = slot<Task>()
        coEvery { repository.insertTask(capture(inserted)) } returns 42L

        AddTaskUseCase(repository, syncTaskReminder, clock)(1L, "Call", null, Priority.HIGH, null, LocalTime.of(9, 30))

        assertThat(inserted.captured.dueTime).isNull()
    }

    @Test
    fun `adding a task whose reminder time already passed does not notify immediately`() = runTest {
        val inserted = slot<Task>()
        coEvery { repository.insertTask(capture(inserted)) } returns 42L

        AddTaskUseCase(repository, syncTaskReminder, clock)(1L, "Call", null, Priority.HIGH, LocalDate.of(2026, 3, 10), LocalTime.of(9, 0))

        assertThat(inserted.captured.isReminderShown).isTrue()
    }

    @Test
    fun `updating a task writes only its editable fields`() = runTest {
        coEvery { repository.getTaskById(4L) } returns existingTask
        val updated = slot<Task>()
        coEvery { repository.updateTaskDetails(capture(updated)) } returns Unit

        updateTask(4L, 2L, "New", "New desc", Priority.HIGH, LocalDate.of(2026, 3, 1), LocalTime.of(8, 0))

        assertThat(updated.captured.taskListId).isEqualTo(2L)
        assertThat(updated.captured.name).isEqualTo("New")
        assertThat(updated.captured.description).isEqualTo("New desc")
        assertThat(updated.captured.priority).isEqualTo(Priority.HIGH)
        coVerify(exactly = 0) { repository.setReminderShown(any(), any()) }
        coVerify(exactly = 0) { repository.toggleTaskDone(any(), any(), any()) }
    }

    @Test
    fun `editing a task without changing its due date only syncs its reminder`() = runTest {
        coEvery { repository.getTaskById(4L) } returns existingTask

        updateTask(4L, 1L, "Renamed", null, Priority.LOW, LocalDate.of(2026, 3, 1), LocalTime.of(8, 0))

        coVerifyOrder {
            repository.updateTaskDetails(any())
            syncTaskReminder(4L)
        }
        coVerify(exactly = 0) { resetTaskReminder(any()) }
    }

    @Test
    fun `rescheduling a task resets its reminder`() = runTest {
        coEvery { repository.getTaskById(4L) } returns existingTask

        updateTask(4L, 1L, "Old", null, Priority.LOW, LocalDate.of(2026, 3, 11), LocalTime.of(8, 0))

        coVerifyOrder {
            repository.updateTaskDetails(any())
            resetTaskReminder(4L)
        }
        coVerify(exactly = 0) { syncTaskReminder(any()) }
    }

    @Test
    fun `updating a task that no longer exists does nothing`() = runTest {
        coEvery { repository.getTaskById(4L) } returns null

        updateTask(4L, 2L, "New", null, Priority.HIGH, null, null)

        coVerify(exactly = 0) { repository.updateTaskDetails(any()) }
        coVerify(exactly = 0) { syncTaskReminder(any()) }
        coVerify(exactly = 0) { resetTaskReminder(any()) }
    }

    @Test
    fun `marking a task done records the completion time and syncs its reminder`() = runTest {
        toggleTaskDone(4L, true)

        coVerifyOrder {
            repository.toggleTaskDone(4L, true, now.toEpochMilli())
            syncTaskReminder(4L)
        }
    }

    @Test
    fun `reopening a task clears the completion time and resets its reminder`() = runTest {
        toggleTaskDone(4L, false)

        coVerifyOrder {
            repository.toggleTaskDone(4L, false, null)
            resetTaskReminder(4L)
        }
        coVerify(exactly = 0) { syncTaskReminder(any()) }
    }

    @Test
    fun `permanently deleting a task syncs its reminder afterwards`() = runTest {
        PermanentlyDeleteTaskUseCase(repository, syncTaskReminder)(4L)

        coVerifyOrder {
            repository.permanentlyDeleteTask(4L)
            syncTaskReminder(4L)
        }
    }
}
