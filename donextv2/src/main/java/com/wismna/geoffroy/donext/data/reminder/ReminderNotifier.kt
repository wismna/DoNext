package com.wismna.geoffroy.donext.data.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    /** Returns false when the notification could not be posted (missing permission). */
    fun showTaskReminder(task: Task): Boolean {
        val taskId = task.id ?: return false
        val dueTimeText = task.dueTime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        val notification = baseBuilder(TASK_CHANNEL_ID)
            .setContentTitle(task.name)
            .setContentText(dueTimeText?.let { context.getString(R.string.notification_task_due_at, it) })
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notification_action_done),
                ReminderIntents.markDone(context, taskId)
            )
            .build()
        return notifyIfAllowed(TASK_TAG, taskId.toInt(), notification)
    }

    fun showDailyDigest(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            notificationManager.cancel(DIGEST_TAG, DIGEST_NOTIFICATION_ID)
            return
        }
        val title = context.resources.getQuantityString(R.plurals.notification_digest_title, tasks.size, tasks.size)
        val style = NotificationCompat.InboxStyle()
        tasks.take(MAX_DIGEST_LINES).forEach { style.addLine(it.name) }
        val notification = baseBuilder(DIGEST_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(tasks.joinToString { it.name })
            .setStyle(style)
            .build()
        notifyIfAllowed(DIGEST_TAG, DIGEST_NOTIFICATION_ID, notification)
    }

    fun dismissTaskReminder(taskId: Long) {
        notificationManager.cancel(TASK_TAG, taskId.toInt())
    }

    private fun baseBuilder(channelId: String): NotificationCompat.Builder {
        ensureChannels()
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(ReminderIntents.openApp(context))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
    }

    private fun notifyIfAllowed(tag: String, id: Int, notification: Notification): Boolean {
        if (!canPostNotifications()) return false
        notificationManager.notify(tag, id, notification)
        return true
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                TASK_CHANNEL_ID,
                context.getString(R.string.notification_channel_tasks),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                DIGEST_CHANNEL_ID,
                context.getString(R.string.notification_channel_digest),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private companion object {
        const val TASK_CHANNEL_ID = "task_reminders"
        const val DIGEST_CHANNEL_ID = "daily_digest"
        const val TASK_TAG = "task"
        const val DIGEST_TAG = "digest"
        const val DIGEST_NOTIFICATION_ID = 0
        const val MAX_DIGEST_LINES = 5
    }
}
