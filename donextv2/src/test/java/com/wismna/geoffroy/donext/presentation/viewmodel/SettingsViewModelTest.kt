package com.wismna.geoffroy.donext.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.usecase.CanScheduleExactRemindersUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetDailyReminderTimeUseCase
import com.wismna.geoffroy.donext.domain.usecase.SetDailyReminderTimeUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dailyReminderTime = MutableStateFlow<LocalTime?>(LocalTime.of(8, 30))
    private val getDailyReminderTime = mockk<GetDailyReminderTimeUseCase>()
    private val setDailyReminderTime = mockk<SetDailyReminderTimeUseCase>(relaxed = true)
    private val canScheduleExactReminders = mockk<CanScheduleExactRemindersUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { getDailyReminderTime() } returns dailyReminderTime
        every { canScheduleExactReminders() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SettingsViewModel(getDailyReminderTime, setDailyReminderTime, canScheduleExactReminders)

    @Test
    fun `exposes the saved daily reminder time`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.dailyReminderTime).isEqualTo(LocalTime.of(8, 30))
        assertThat(viewModel.isDailyReminderEnabled).isTrue()
    }

    @Test
    fun `turning the daily reminder off saves null`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDailyReminderToggled(false)
        advanceUntilIdle()

        coVerify { setDailyReminderTime(null) }
    }

    @Test
    fun `turning the daily reminder back on restores the last enabled time`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        dailyReminderTime.value = null
        advanceUntilIdle()

        viewModel.onDailyReminderToggled(true)
        advanceUntilIdle()

        coVerify { setDailyReminderTime(LocalTime.of(8, 30)) }
    }

    @Test
    fun `turning the daily reminder on for the first time uses 9 AM`() = runTest {
        dailyReminderTime.value = null
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDailyReminderToggled(true)
        advanceUntilIdle()

        coVerify { setDailyReminderTime(LocalTime.of(9, 0)) }
    }

    @Test
    fun `changing the daily reminder time saves it`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onDailyReminderTimeChanged(LocalTime.of(20, 15))
        advanceUntilIdle()

        coVerify { setDailyReminderTime(LocalTime.of(20, 15)) }
    }

    @Test
    fun `refreshing picks up a newly granted exact alarm permission`() = runTest {
        every { canScheduleExactReminders() } returns false
        val viewModel = createViewModel()
        assertThat(viewModel.canUseExactReminders).isFalse()

        every { canScheduleExactReminders() } returns true
        viewModel.refreshExactReminderPermission()

        assertThat(viewModel.canUseExactReminders).isTrue()
    }

    @Test
    fun `is not loaded until the saved time is emitted`() = runTest {
        val pendingTime = MutableSharedFlow<LocalTime?>()
        every { getDailyReminderTime() } returns pendingTime
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertThat(viewModel.isLoaded).isFalse()

        pendingTime.emit(LocalTime.of(18, 0))
        advanceUntilIdle()

        assertThat(viewModel.isLoaded).isTrue()
        assertThat(viewModel.dailyReminderTime).isEqualTo(LocalTime.of(18, 0))
    }
}
