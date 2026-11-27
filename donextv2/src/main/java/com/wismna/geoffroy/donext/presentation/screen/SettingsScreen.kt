package com.wismna.geoffroy.donext.presentation.screen

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.presentation.viewmodel.SettingsViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(hasNotificationPermission(context)) }
    val requestNotificationPermission = rememberNotificationPermissionRequester { notificationsAllowed = it }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshExactReminderPermission()
        notificationsAllowed = hasNotificationPermission(context)
        onPauseOrDispose { }
    }

    Column(modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(R.string.settings_reminders_header),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        if (!notificationsAllowed) {
            NotificationsBlockedRow()
        }
        DailySummaryToggle(viewModel, onEnabled = requestNotificationPermission)
        if (viewModel.isDailyReminderEnabled) {
            DailySummaryTime(viewModel)
        }
        if (!viewModel.canUseExactReminders) {
            ExactRemindersRow()
        }
    }
}

@Composable
private fun DailySummaryToggle(viewModel: SettingsViewModel, onEnabled: () -> Unit) {
    val onToggled = { enabled: Boolean ->
        viewModel.onDailyReminderToggled(enabled)
        if (enabled) onEnabled()
    }
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_daily_summary)) },
        supportingContent = { Text(stringResource(R.string.settings_daily_summary_description)) },
        trailingContent = {
            Switch(
                checked = viewModel.isDailyReminderEnabled,
                onCheckedChange = onToggled,
                enabled = viewModel.isLoaded
            )
        },
        colors = transparentListItemColors(),
        modifier = Modifier.clickable(enabled = viewModel.isLoaded) { onToggled(!viewModel.isDailyReminderEnabled) }
    )
}

@Composable
private fun DailySummaryTime(viewModel: SettingsViewModel) {
    val time = viewModel.dailyReminderTime ?: return
    var showTimePicker by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_daily_summary_time)) },
        supportingContent = { Text(time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))) },
        colors = transparentListItemColors(),
        modifier = Modifier.clickable { showTimePicker = true }
    )

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onConfirm = {
                viewModel.onDailyReminderTimeChanged(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun NotificationsBlockedRow() {
    val context = LocalContext.current

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_notifications_blocked)) },
        supportingContent = { Text(stringResource(R.string.settings_notifications_blocked_description)) },
        colors = transparentListItemColors(),
        modifier = Modifier.clickable {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
        }
    )
}

@Composable
private fun ExactRemindersRow() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val context = LocalContext.current

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_exact_reminders)) },
        supportingContent = { Text(stringResource(R.string.settings_exact_reminders_description)) },
        colors = transparentListItemColors(),
        modifier = Modifier.clickable {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri())
            context.startActivity(intent)
        }
    )
}

@Composable
private fun transparentListItemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)
