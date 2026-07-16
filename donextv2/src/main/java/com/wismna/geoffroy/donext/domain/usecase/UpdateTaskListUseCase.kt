package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import javax.inject.Inject

class UpdateTaskListUseCase @Inject constructor(
    private val repository: TaskListRepository
) {
    suspend operator fun invoke(taskListId: Long, title: String, order: Int) {
        repository.updateTaskList(
            TaskList(
                id = taskListId,
                name = title,
                order = order,
                isDeleted = false
            )
        )
    }
}