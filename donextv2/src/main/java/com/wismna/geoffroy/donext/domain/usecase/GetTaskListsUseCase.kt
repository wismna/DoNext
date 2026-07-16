package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.repository.TaskListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskListsUseCase @Inject constructor(private val repository: TaskListRepository) {
    operator fun invoke(): Flow<List<TaskList>> = repository.getTaskLists()
}