package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.withReminderReset
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(
        taskListId: Long,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDate?,
        dueTime: LocalTime?
    ) {
        val taskId = repository.insertTask(
            Task(
                taskListId = taskListId,
                name = title,
                description = description ?: "",
                isDeleted = false,
                isDone = false,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime.takeIf { dueDate != null },
                createdAt = clock.millis()
            ).withReminderReset(clock)
        )
        syncTaskReminder(taskId)
    }
}
