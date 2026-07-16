package com.wismna.geoffroy.donext.data.local.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

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
}
