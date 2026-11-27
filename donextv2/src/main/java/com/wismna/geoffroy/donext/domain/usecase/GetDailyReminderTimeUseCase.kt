package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import javax.inject.Inject

class GetDailyReminderTimeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<LocalTime?> = settingsRepository.getDailyReminderTime()
}
