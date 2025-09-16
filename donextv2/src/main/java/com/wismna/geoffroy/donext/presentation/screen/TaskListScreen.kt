package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TaskListScreen(modifier: Modifier = Modifier, viewModel: TaskListViewModel = hiltViewModel<TaskListViewModel>(), onTaskClick: (Task) -> Unit) {
    val tasks = viewModel.tasks

    LazyColumn(
        modifier = modifier.fillMaxSize().padding()
    ) {
        itemsIndexed(tasks, key = { _, task -> task.id!! }) { index, task ->
            if (index > 0) {
                val prev = tasks[index - 1]

                when {
                    // Divider between non-done and done tasks
                    !prev.isDone && task.isDone -> {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Extra spacing between different priorities (only if done status is same)
                    prev.priority != task.priority && prev.isDone == task.isDone -> {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            TaskItem(
                task = task,
                onClick = { onTaskClick(task) },
                onToggleDone = { isChecked ->
                    viewModel.updateTaskDone(task.id!!, isChecked)
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit
) {
    val today = remember {
        LocalDate.now(ZoneOffset.UTC)
    }
    val isOverdue = task.dueDate?.let { millis ->
        val dueDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        dueDate.isBefore(today)
    } ?: false

    val baseStyle = MaterialTheme.typography.bodyLarge.copy(
    fontWeight = when (task.priority) {
        Priority.HIGH -> FontWeight.Bold
        Priority.NORMAL -> FontWeight.Normal
        Priority.LOW -> FontWeight.Normal
    },
    color = when (task.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.onSurface
        Priority.NORMAL -> MaterialTheme.colorScheme.onSurface
        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    },
    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None)

    val dueText = task.dueDate?.let { formatTaskDueDate(it) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .alpha(if (task.isDone || task.priority == Priority.LOW) 0.5f else 1f),
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = onToggleDone,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = task.name,
                    style = baseStyle
                )
                // Due date badge
                dueText?.let {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.1f
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    style = baseStyle.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatTaskDueDate(dueMillis: Long): String {
    val today = LocalDate.now()
    val dueDate = Instant.ofEpochMilli(dueMillis).atZone(ZoneOffset.UTC).toLocalDate()

    return when {
        dueDate.isEqual(today) -> "Today"
        dueDate.isEqual(today.plusDays(1)) -> "Tomorrow"
        dueDate.isEqual(today.minusDays(1)) -> "Yesterday"
        dueDate.isAfter(today) && dueDate.isBefore(today.plusDays(7)) ->
            dueDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        else ->
            dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
    }
}