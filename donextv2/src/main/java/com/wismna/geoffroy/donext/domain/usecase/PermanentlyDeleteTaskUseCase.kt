package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class PermanentlyDeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase
) {
    suspend operator fun invoke(taskId: Long) {
        repository.permanentlyDeleteTask(taskId)
        syncTaskReminder(taskId)
    }
}
