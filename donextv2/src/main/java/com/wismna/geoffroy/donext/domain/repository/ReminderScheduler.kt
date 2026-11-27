package com.wismna.geoffroy.donext.domain.repository

import java.time.Instant

interface ReminderScheduler {
    fun scheduleTaskReminder(taskId: Long, at: Instant)
    fun cancelTaskReminder(taskId: Long)
    fun dismissTaskReminder(taskId: Long)
    fun scheduleDailyDigest(at: Instant)
    fun cancelDailyDigest()
    fun canScheduleExactReminders(): Boolean
}
