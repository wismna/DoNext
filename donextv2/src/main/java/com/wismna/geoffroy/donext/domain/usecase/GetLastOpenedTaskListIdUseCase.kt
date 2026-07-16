package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import javax.inject.Inject

class GetLastOpenedTaskListIdUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Long? = repository.getLastOpenedTaskListId()
}
