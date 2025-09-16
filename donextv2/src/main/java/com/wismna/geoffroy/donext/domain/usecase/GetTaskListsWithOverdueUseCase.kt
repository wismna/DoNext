package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class GetTaskListsWithOverdueUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<TaskListWithOverdue>> {
        return taskRepository.getTaskListsWithOverdue(Instant.parse("2025-09-15T12:00:00Z").toEpochMilli())
    }
}