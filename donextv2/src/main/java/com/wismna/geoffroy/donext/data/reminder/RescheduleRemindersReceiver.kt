package com.wismna.geoffroy.donext.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wismna.geoffroy.donext.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.util.TimeZone
import javax.inject.Inject

/** Alarms are lost on reboot and app update, and shift with clock or timezone changes. */
@AndroidEntryPoint
class RescheduleRemindersReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleAllReminders: RescheduleAllRemindersUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            // The cached default zone may not be refreshed yet when this broadcast arrives
            TimeZone.setDefault(null)
        }
        launchAsync { rescheduleAllReminders() }
    }
}
