package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Updates the editable fields only: the done state, timestamps and reminder state belong to other use cases. */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val resetTaskReminder: ResetTaskReminderUseCase
) {
    suspend operator fun invoke(
        taskId: Long,
        taskListId: Long,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDate?,
        dueTime: LocalTime?
    ) {
        val existingTask = repository.getTaskById(taskId) ?: return
        val updatedTask = existingTask.copy(
            taskListId = taskListId,
            name = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueTime = dueTime.takeIf { dueDate != null }
        )
        repository.updateTaskDetails(updatedTask)

        if (updatedTask.isRescheduledFrom(existingTask)) {
            resetTaskReminder(taskId)
        } else {
            syncTaskReminder(taskId)
        }
    }

    private fun Task.isRescheduledFrom(previous: Task): Boolean =
        dueDate != previous.dueDate || dueTime != previous.dueTime
}
