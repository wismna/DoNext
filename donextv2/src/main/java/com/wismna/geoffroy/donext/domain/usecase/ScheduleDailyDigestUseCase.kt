package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class ScheduleDailyDigestUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke() {
        val digestTime = settingsRepository.getDailyReminderTime().first()
        if (digestTime == null) {
            reminderScheduler.cancelDailyDigest()
            settingsRepository.setPendingDigestDate(null)
            return
        }

        val today = LocalDate.now(clock)
        val todayAt = today.at(digestTime)
        when {
            todayAt.isAfter(clock.instant()) -> schedule(today, todayAt)
            // Today's digest was due but its alarm has not fired yet: show it now rather than skip a day
            settingsRepository.getPendingDigestDate() == today -> reminderScheduler.scheduleDailyDigest(clock.instant())
            else -> schedule(today.plusDays(1), today.plusDays(1).at(digestTime))
        }
    }

    private suspend fun schedule(date: LocalDate, at: Instant) {
        settingsRepository.setPendingDigestDate(date)
        reminderScheduler.scheduleDailyDigest(at)
    }

    private fun LocalDate.at(time: LocalTime): Instant = atTime(time).atZone(clock.zone).toInstant()
}
