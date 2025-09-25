package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeletedTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getDeletedTasks()
}