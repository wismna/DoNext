package com.wismna.geoffroy.donext.domain.repository

interface SettingsRepository {
    suspend fun getLastOpenedTaskListId(): Long?
    suspend fun setLastOpenedTaskListId(taskListId: Long)
}
