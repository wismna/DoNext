package com.wismna.geoffroy.donext.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.wismna.geoffroy.donext.data.toLocalTime
import com.wismna.geoffroy.donext.data.toMinutesOfDay
import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    private val preferences: Flow<Preferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override suspend fun getLastOpenedTaskListId(): Long? =
        preferences.first()[LAST_OPENED_TASK_LIST_ID_KEY]

    override suspend fun setLastOpenedTaskListId(taskListId: Long) {
        dataStore.edit { prefs -> prefs[LAST_OPENED_TASK_LIST_ID_KEY] = taskListId }
    }

    override fun getDailyReminderTime(): Flow<LocalTime?> =
        preferences.map { prefs ->
            val minutes = prefs[DAILY_REMINDER_MINUTES_KEY] ?: DEFAULT_DAILY_REMINDER.toMinutesOfDay()
            if (minutes == DAILY_REMINDER_DISABLED) null else minutes.toLocalTime()
        }

    override suspend fun setDailyReminderTime(time: LocalTime?) {
        dataStore.edit { prefs ->
            prefs[DAILY_REMINDER_MINUTES_KEY] = time?.toMinutesOfDay() ?: DAILY_REMINDER_DISABLED
        }
    }

    override suspend fun getPendingDigestDate(): LocalDate? =
        preferences.first()[PENDING_DIGEST_EPOCH_DAY_KEY]?.let { LocalDate.ofEpochDay(it) }

    override suspend fun setPendingDigestDate(date: LocalDate?) {
        dataStore.edit { prefs ->
            if (date == null) {
                prefs.remove(PENDING_DIGEST_EPOCH_DAY_KEY)
            } else {
                prefs[PENDING_DIGEST_EPOCH_DAY_KEY] = date.toEpochDay()
            }
        }
    }

    private companion object {
        val LAST_OPENED_TASK_LIST_ID_KEY = longPreferencesKey("last_opened_task_list_id")
        val DAILY_REMINDER_MINUTES_KEY = intPreferencesKey("daily_reminder_minutes")
        val PENDING_DIGEST_EPOCH_DAY_KEY = longPreferencesKey("pending_digest_epoch_day")
        val DEFAULT_DAILY_REMINDER: LocalTime = LocalTime.of(9, 0)
        const val DAILY_REMINDER_DISABLED = -1
    }
}
