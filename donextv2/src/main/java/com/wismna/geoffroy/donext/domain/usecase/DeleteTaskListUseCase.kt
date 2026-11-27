package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskListUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskListRepository: TaskListRepository,
    private val syncTaskReminder: SyncTaskReminderUseCase,
    private val resetTaskReminder: ResetTaskReminderUseCase
) {
    suspend operator fun invoke(taskListId: Long, isDeleted: Boolean) {
        taskRepository.toggleAllTasksInListDeleted(taskListId, isDeleted)
        taskListRepository.deleteTaskList(taskListId, isDeleted)
        taskRepository.getTaskIdsForList(taskListId).forEach { taskId ->
            if (isDeleted) syncTaskReminder(taskId) else resetTaskReminder(taskId)
        }
    }
}
