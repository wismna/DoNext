package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class ToggleTaskDeletedUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isDeleted: Boolean) {
        if (!isDeleted) {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                // If task list was soft-deleted, restore it as well
                val taskList = repository.getTaskListById(task.taskListId)
                if (taskList != null && taskList.isDeleted) {
                    repository.updateTaskList(taskList.copy(isDeleted = false))
                }
            }
        }

        repository.toggleTaskDeleted(taskId, isDeleted)
    }
}