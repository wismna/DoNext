package com.wismna.geoffroy.donext.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.wismna.geoffroy.donext.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override suspend fun getLastOpenedTaskListId(): Long? =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()[LAST_OPENED_TASK_LIST_ID_KEY]

    override suspend fun setLastOpenedTaskListId(taskListId: Long) {
        dataStore.edit { prefs -> prefs[LAST_OPENED_TASK_LIST_ID_KEY] = taskListId }
    }

    private companion object {
        val LAST_OPENED_TASK_LIST_ID_KEY = longPreferencesKey("last_opened_task_list_id")
    }
}
