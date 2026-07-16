package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetTaskListsWithOverdueUseCase @Inject constructor(
    private val taskListRepository: TaskListRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<TaskListWithOverdue>> {
        return taskListRepository.getTaskListsWithOverdue(
            LocalDate.now(clock)
                .atStartOfDay(clock.zone)
                .toInstant()
                .toEpochMilli()
        )
    }
}