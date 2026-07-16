package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetTaskListsWithOverdueUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<TaskListWithOverdue>> {
        return taskRepository.getTaskListsWithOverdue(
            LocalDate.now(clock)
                .atStartOfDay(clock.zone)
                .toInstant()
                .toEpochMilli()
        )
    }
}