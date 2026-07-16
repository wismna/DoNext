package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class ToggleTaskDeletedUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskListRepository: TaskListRepository
) {
    suspend operator fun invoke(taskId: Long, isDeleted: Boolean) {
        if (!isDeleted) {
            val task = taskRepository.getTaskById(taskId)
            if (task != null) {
                // If task list was soft-deleted, restore it as well
                val taskList = taskListRepository.getTaskListById(task.taskListId)
                if (taskList != null && taskList.isDeleted) {
                    taskListRepository.updateTaskList(taskList.copy(isDeleted = false))
                }
            }
        }

        taskRepository.toggleTaskDeleted(taskId, isDeleted)
    }
}
