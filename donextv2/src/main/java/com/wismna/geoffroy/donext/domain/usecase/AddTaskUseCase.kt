package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskListId: Long, title: String, description: String?) {
        repository.insertTask(
            Task(
                taskListId = taskListId,
                name = title,
                description = description ?: "",
                isDeleted = false,
                cycle = 0,
                isDone = false,
                priority = 0,
                order = 0
            )
        )
    }
}