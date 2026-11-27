package com.wismna.geoffroy.donext.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wismna.geoffroy.donext.domain.usecase.ConsumeTaskReminderUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetDigestTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.MarkDailyDigestShownUseCase
import com.wismna.geoffroy.donext.domain.usecase.ScheduleDailyDigestUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDoneUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var consumeTaskReminder: ConsumeTaskReminderUseCase
    @Inject lateinit var getDigestTasks: GetDigestTasksUseCase
    @Inject lateinit var markDailyDigestShown: MarkDailyDigestShownUseCase
    @Inject lateinit var scheduleDailyDigest: ScheduleDailyDigestUseCase
    @Inject lateinit var toggleTaskDone: ToggleTaskDoneUseCase
    @Inject lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderIntents.EXTRA_TASK_ID, NO_TASK_ID)
        when (intent.action) {
            ReminderIntents.ACTION_TASK_REMINDER -> launchAsync { showTaskReminder(taskId) }
            ReminderIntents.ACTION_MARK_DONE -> launchAsync { markDone(taskId) }
            ReminderIntents.ACTION_DAILY_DIGEST -> launchAsync { showDailyDigest() }
        }
    }

    private suspend fun showTaskReminder(taskId: Long) {
        consumeTaskReminder(taskId) { task -> notifier.showTaskReminder(task) }
    }

    private suspend fun markDone(taskId: Long) {
        toggleTaskDone(taskId, true)
    }

    private suspend fun showDailyDigest() {
        notifier.showDailyDigest(getDigestTasks())
        markDailyDigestShown()
        scheduleDailyDigest()
    }

    private companion object {
        const val NO_TASK_ID = -1L
    }
}
