package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskListUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(title: String, order: Int) {
        repository.insertTaskList(
            TaskList(
                name = title,
                order = order,
                isDeleted = false
            )
        )
    }
}