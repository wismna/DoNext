package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskListsWithOverdueUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<TaskListWithOverdue>> {
        return taskRepository.getTaskListsWithOverdue()
    }
}