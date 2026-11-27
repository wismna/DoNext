package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.reminderInstant
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import javax.inject.Inject

class SyncTaskReminderUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke(taskId: Long) {
        val task = taskRepository.getTaskById(taskId)
        if (task == null || task.isDone || task.isDeleted) {
            reminderScheduler.cancelTaskReminder(taskId)
            reminderScheduler.dismissTaskReminder(taskId)
            return
        }
        scheduleOrCancel(taskId, task)
    }

    private fun scheduleOrCancel(taskId: Long, task: Task) {
        val reminderAt = task.reminderInstant(clock)
        if (reminderAt == null || task.isReminderShown) {
            reminderScheduler.cancelTaskReminder(taskId)
            return
        }
        // A due reminder that was never shown (delayed alarm, device off) fires right away
        val now = clock.instant()
        reminderScheduler.scheduleTaskReminder(taskId, if (reminderAt.isAfter(now)) reminderAt else now)
    }
}
