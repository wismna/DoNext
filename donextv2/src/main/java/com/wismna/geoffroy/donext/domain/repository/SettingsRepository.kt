package com.wismna.geoffroy.donext.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

interface SettingsRepository {
    suspend fun getLastOpenedTaskListId(): Long?
    suspend fun setLastOpenedTaskListId(taskListId: Long)

    /** Time of the daily digest notification, or null when the digest is disabled. */
    fun getDailyReminderTime(): Flow<LocalTime?>
    suspend fun setDailyReminderTime(time: LocalTime?)

    /** Day the digest alarm is scheduled for and has not been shown yet, if any. */
    suspend fun getPendingDigestDate(): LocalDate?
    suspend fun setPendingDigestDate(date: LocalDate?)
}
