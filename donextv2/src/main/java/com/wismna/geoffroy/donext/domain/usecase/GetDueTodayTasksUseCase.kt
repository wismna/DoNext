package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetDueTodayTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<Task>> {
        val today = LocalDate.now(clock)
        val todayStart = today
            .atStartOfDay(clock.zone)
            .toInstant()
            .toEpochMilli()

        val todayEnd = today
            .plusDays(1)
            .atStartOfDay(clock.zone)
            .toInstant()
            .toEpochMilli() - 1
        return repository.getDueTodayTasks(
            todayStart, todayEnd
        )
    }
}