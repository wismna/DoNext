package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class RescheduleAllRemindersUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val scheduleDailyDigest: ScheduleDailyDigestUseCase
) {
    suspend operator fun invoke() {
        taskRepository.getOpenTasksWithDueTime()
            .mapNotNull { it.id }
            .forEach { syncTaskReminder(it) }
        scheduleDailyDigest()
    }
}
