package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.withReminderReset
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject

/**
 * For a task whose reminder becomes relevant again (rescheduled, reopened or restored): dismisses the
 * notification of its previous reminder, re-arms a future reminder and treats a past one as shown,
 * so it never pops up late.
 */
class ResetTaskReminderUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: Long) {
        reminderScheduler.dismissTaskReminder(taskId)
        taskRepository.getTaskById(taskId)?.let { task ->
            taskRepository.setReminderShown(taskId, task.withReminderReset(clock).isReminderShown)
        }
        syncTaskReminder(taskId)
    }
}
