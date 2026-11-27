package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import java.time.LocalTime
import javax.inject.Inject

class SetDailyReminderTimeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduleDailyDigest: ScheduleDailyDigestUseCase
) {
    suspend operator fun invoke(time: LocalTime?) {
        settingsRepository.setDailyReminderTime(time)
        // A new time starts fresh: the catch-up for a late alarm must not show the digest right away
        settingsRepository.setPendingDigestDate(null)
        scheduleDailyDigest()
    }
}
