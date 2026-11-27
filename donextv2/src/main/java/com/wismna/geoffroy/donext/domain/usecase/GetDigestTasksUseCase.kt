package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.extension.sortedForDisplay
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Overdue tasks, plus tasks due today without a time (timed tasks get their own reminder). */
class GetDigestTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(): List<Task> {
        val today = LocalDate.now(clock)
        return taskRepository.getOpenTasksDueBy(today)
            .filter { task -> isOverdue(task, today) || task.dueTime == null }
            .sortedForDisplay(clock)
    }

    private fun isOverdue(task: Task, today: LocalDate): Boolean =
        task.dueDate != null && task.dueDate.isBefore(today)
}
