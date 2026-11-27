package com.wismna.geoffroy.donext.data.local.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(): SettingsRepositoryImpl {
        // DataStore's internal actor coroutine runs on this scope. A plain TestScope() uses
        // StandardTestDispatcher, which queues work that only runs when its own scheduler is
        // advanced — but nothing here ever advances it, so reads/writes deadlock forever.
        // UnconfinedTestDispatcher runs dispatched coroutines eagerly instead, so the actor
        // makes progress without needing to be pumped.
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher()),
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        return SettingsRepositoryImpl(testDataStore)
    }

    @Test
    fun `getLastOpenedTaskListId returns null when nothing saved`() = runTest {
        assertThat(createRepository().getLastOpenedTaskListId()).isNull()
    }

    @Test
    fun `setLastOpenedTaskListId then get returns saved value`() = runTest {
        val repo = createRepository()
        repo.setLastOpenedTaskListId(42L)
        assertThat(repo.getLastOpenedTaskListId()).isEqualTo(42L)
    }

    @Test
    fun `daily reminder defaults to 9 AM when nothing saved`() = runTest {
        assertThat(createRepository().getDailyReminderTime().first()).isEqualTo(LocalTime.of(9, 0))
    }

    @Test
    fun `setDailyReminderTime then get returns saved time`() = runTest {
        val repo = createRepository()
        repo.setDailyReminderTime(LocalTime.of(18, 45))
        assertThat(repo.getDailyReminderTime().first()).isEqualTo(LocalTime.of(18, 45))
    }

    @Test
    fun `disabling the daily reminder returns null`() = runTest {
        val repo = createRepository()
        repo.setDailyReminderTime(null)
        assertThat(repo.getDailyReminderTime().first()).isNull()
    }

    @Test
    fun `pending digest date round-trips and can be cleared`() = runTest {
        val repo = createRepository()
        assertThat(repo.getPendingDigestDate()).isNull()

        repo.setPendingDigestDate(LocalDate.of(2026, 3, 10))
        assertThat(repo.getPendingDigestDate()).isEqualTo(LocalDate.of(2026, 3, 10))

        repo.setPendingDigestDate(null)
        assertThat(repo.getPendingDigestDate()).isNull()
    }
}
