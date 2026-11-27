package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ScheduleDailyDigestUseCaseTest {

    private val zone = ZoneId.of("America/Montreal")
    private val today = LocalDate.of(2026, 3, 10)
    private val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)

    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val reminderScheduler = mockk<ReminderScheduler>(relaxed = true)
    private val useCase = ScheduleDailyDigestUseCase(settingsRepository, reminderScheduler, clock)

    private fun givenDigestTime(time: LocalTime?, pendingDate: LocalDate? = null) {
        every { settingsRepository.getDailyReminderTime() } returns flowOf(time)
        coEvery { settingsRepository.getPendingDigestDate() } returns pendingDate
    }

    @Test
    fun `schedules today when the digest time is still ahead`() = runTest {
        givenDigestTime(LocalTime.of(18, 0))

        useCase()

        verify { reminderScheduler.scheduleDailyDigest(today.atTime(18, 0).atZone(zone).toInstant()) }
        coVerify { settingsRepository.setPendingDigestDate(today) }
    }

    @Test
    fun `schedules tomorrow when today's digest was already shown`() = runTest {
        givenDigestTime(LocalTime.of(9, 0), pendingDate = null)

        useCase()

        verify { reminderScheduler.scheduleDailyDigest(today.plusDays(1).atTime(9, 0).atZone(zone).toInstant()) }
        coVerify { settingsRepository.setPendingDigestDate(today.plusDays(1)) }
    }

    @Test
    fun `fires today's digest right away when its alarm is late`() = runTest {
        givenDigestTime(LocalTime.of(9, 0), pendingDate = today)

        useCase()

        verify { reminderScheduler.scheduleDailyDigest(clock.instant()) }
        coVerify(exactly = 0) { settingsRepository.setPendingDigestDate(any()) }
    }

    @Test
    fun `does not catch up a digest missed on a previous day`() = runTest {
        givenDigestTime(LocalTime.of(9, 0), pendingDate = today.minusDays(1))

        useCase()

        verify { reminderScheduler.scheduleDailyDigest(today.plusDays(1).atTime(9, 0).atZone(zone).toInstant()) }
    }

    @Test
    fun `schedules tomorrow when the digest time is exactly now and nothing is pending`() = runTest {
        givenDigestTime(LocalTime.of(10, 0))

        useCase()

        verify { reminderScheduler.scheduleDailyDigest(today.plusDays(1).atTime(10, 0).atZone(zone).toInstant()) }
    }

    @Test
    fun `cancels the digest and clears its pending date when disabled`() = runTest {
        givenDigestTime(null)

        useCase()

        verify { reminderScheduler.cancelDailyDigest() }
        coVerify { settingsRepository.setPendingDigestDate(null) }
        verify(exactly = 0) { reminderScheduler.scheduleDailyDigest(any()) }
    }
}
