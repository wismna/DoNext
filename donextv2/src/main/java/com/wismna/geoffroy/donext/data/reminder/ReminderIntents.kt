package com.wismna.geoffroy.donext.data.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * PendingIntents are matched on action + data (not extras), so each task gets its own data URI
 * to keep one alarm per task.
 */
object ReminderIntents {
    const val ACTION_TASK_REMINDER = "com.wismna.geoffroy.donext.action.TASK_REMINDER"
    const val ACTION_DAILY_DIGEST = "com.wismna.geoffroy.donext.action.DAILY_DIGEST"
    const val ACTION_MARK_DONE = "com.wismna.geoffroy.donext.action.MARK_DONE"
    const val EXTRA_TASK_ID = "taskId"

    private const val FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun taskReminder(context: Context, taskId: Long): PendingIntent =
        broadcast(context, ACTION_TASK_REMINDER, taskId)

    fun markDone(context: Context, taskId: Long): PendingIntent =
        broadcast(context, ACTION_MARK_DONE, taskId)

    fun dailyDigest(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_DAILY_DIGEST)
        return PendingIntent.getBroadcast(context, 0, intent, FLAGS)
    }

    fun openApp(context: Context): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        return PendingIntent.getActivity(context, 0, launchIntent, FLAGS)
    }

    private fun broadcast(context: Context, action: String, taskId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .setData("donext://task/$taskId".toUri())
            .putExtra(EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(context, 0, intent, FLAGS)
    }
}
