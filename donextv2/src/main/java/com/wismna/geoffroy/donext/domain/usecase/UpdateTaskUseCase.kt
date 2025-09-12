package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, taskListId: Long, title: String, description: String?, priority: Priority) {
        repository.updateTask(
            Task(
                id = taskId,
                taskListId = taskListId,
                name = title,
                description = description ?: "",
                isDeleted = false,
                isDone = false,
                priority = priority,
            )
        )
    }
}