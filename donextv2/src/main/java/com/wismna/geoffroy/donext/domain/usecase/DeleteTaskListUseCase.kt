package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskListUseCase@Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskListId: Long, isDeleted: Boolean) {
        repository.deleteTaskList(taskListId, isDeleted)
    }
}