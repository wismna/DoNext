package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.QuickAddResult
import com.wismna.geoffroy.donext.presentation.model.formatDueDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Shows what the quick-add shortcuts typed in the title will set when the task is created. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAddChips(preview: QuickAddResult, listName: String?, today: LocalDate, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        preview.dueDate?.let {
            QuickAddChip(formatDueDate(it, today), Icons.Default.Event, stringResource(R.string.task_due_date))
        }
        preview.dueTime?.let {
            val time = it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            QuickAddChip(time, Icons.Default.Schedule, stringResource(R.string.task_due_time))
        }
        preview.priority?.let {
            QuickAddChip(stringResource(it.label), Icons.Default.Flag, stringResource(R.string.task_priority))
        }
        listName?.let {
            QuickAddChip(it, Icons.Default.Checklist, stringResource(R.string.quick_add_list))
        }
    }
}

@Composable
private fun QuickAddChip(label: String, icon: ImageVector, iconDescription: String) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = iconDescription) }
    )
}
