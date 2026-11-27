package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject

class ToggleTaskDoneUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val resetTaskReminder: ResetTaskReminderUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: Long, isDone: Boolean) {
        val completedAt = if (isDone) clock.millis() else null
        repository.toggleTaskDone(taskId, isDone, completedAt)
        if (isDone) syncTaskReminder(taskId) else resetTaskReminder(taskId)
    }
}
