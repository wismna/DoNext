package com.wismna.geoffroy.donext.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class AlarmReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notifier: ReminderNotifier
) : ReminderScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleTaskReminder(taskId: Long, at: Instant) {
        setAlarm(at, ReminderIntents.taskReminder(context, taskId))
    }

    override fun cancelTaskReminder(taskId: Long) {
        alarmManager.cancel(ReminderIntents.taskReminder(context, taskId))
    }

    override fun dismissTaskReminder(taskId: Long) {
        notifier.dismissTaskReminder(taskId)
    }

    override fun scheduleDailyDigest(at: Instant) {
        setAlarm(at, ReminderIntents.dailyDigest(context))
    }

    override fun cancelDailyDigest() {
        alarmManager.cancel(ReminderIntents.dailyDigest(context))
    }

    override fun canScheduleExactReminders(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun setAlarm(at: Instant, operation: PendingIntent) {
        val triggerAtMillis = at.toEpochMilli()
        if (!canScheduleExactReminders()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            return
        }
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } catch (_: SecurityException) {
            // The exact alarm permission was revoked after the check
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }
}
