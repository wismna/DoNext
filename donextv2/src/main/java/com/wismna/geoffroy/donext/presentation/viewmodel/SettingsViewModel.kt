package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.usecase.CanScheduleExactRemindersUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetDailyReminderTimeUseCase
import com.wismna.geoffroy.donext.domain.usecase.SetDailyReminderTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getDailyReminderTime: GetDailyReminderTimeUseCase,
    private val setDailyReminderTime: SetDailyReminderTimeUseCase,
    private val canScheduleExactReminders: CanScheduleExactRemindersUseCase
) : ViewModel() {

    var dailyReminderTime by mutableStateOf<LocalTime?>(null)
        private set

    var canUseExactReminders by mutableStateOf(true)
        private set

    var isLoaded by mutableStateOf(false)
        private set

    val isDailyReminderEnabled: Boolean
        get() = dailyReminderTime != null

    private var lastEnabledTime: LocalTime = DEFAULT_DAILY_REMINDER_TIME

    init {
        getDailyReminderTime()
            .onEach { time ->
                dailyReminderTime = time
                isLoaded = true
                if (time != null) lastEnabledTime = time
            }
            .launchIn(viewModelScope)
        refreshExactReminderPermission()
    }

    fun onDailyReminderToggled(enabled: Boolean) {
        saveDailyReminderTime(if (enabled) lastEnabledTime else null)
    }

    fun onDailyReminderTimeChanged(time: LocalTime) {
        saveDailyReminderTime(time)
    }

    fun refreshExactReminderPermission() {
        canUseExactReminders = canScheduleExactReminders()
    }

    private fun saveDailyReminderTime(time: LocalTime?) {
        viewModelScope.launch { setDailyReminderTime(time) }
    }

    private companion object {
        val DEFAULT_DAILY_REMINDER_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
