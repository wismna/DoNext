package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import javax.inject.Inject

class MarkDailyDigestShownUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.setPendingDigestDate(null)
    }
}
