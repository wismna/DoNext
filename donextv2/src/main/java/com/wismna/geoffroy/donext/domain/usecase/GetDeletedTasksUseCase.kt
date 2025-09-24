package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class GetDeletedTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(): List<Task> = repository.getDeletedTasks()
}