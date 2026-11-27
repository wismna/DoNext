package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.reminderInstant
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject

/**
 * Handles a task alarm: shows the reminder if it is still pending, and records it as shown only when
 * [show] reports that it was displayed (it is not when notifications are blocked), so it can fire again later.
 */
class ConsumeTaskReminderUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: Long, show: (Task) -> Boolean) {
        val task = taskRepository.getTaskById(taskId)
            ?.takeIf { it.reminderInstant(clock) != null && !it.isReminderShown }
            ?: return
        if (show(task)) {
            taskRepository.setReminderShown(taskId, true)
        }
    }
}
