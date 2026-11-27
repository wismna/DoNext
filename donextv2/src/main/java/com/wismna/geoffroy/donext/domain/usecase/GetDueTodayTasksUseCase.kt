package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.sortedForDisplay
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetDueTodayTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: Clock
) {
    operator fun invoke(): Flow<List<Task>> =
        repository.getDueTodayTasks(LocalDate.now(clock))
            .map { it.sortedForDisplay(clock) }
}
