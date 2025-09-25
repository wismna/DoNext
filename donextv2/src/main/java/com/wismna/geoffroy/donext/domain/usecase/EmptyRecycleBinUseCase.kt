package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import javax.inject.Inject

class EmptyRecycleBinUseCase  @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke() {
        repository.permanentlyDeleteAllDeletedTask()
    }
}