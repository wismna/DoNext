package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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

@Composable
fun TaskListScreen(viewModel: TaskListViewModel = hiltViewModel(), onTaskClick: (Task) -> Unit) {
    val tasks = viewModel.tasks

    LazyColumn(
        modifier = Modifier.fillMaxSize()
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
    color = if (isOverdue && !task.isDone) MaterialTheme.colorScheme.error else when (task.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.onSurface
        Priority.NORMAL -> MaterialTheme.colorScheme.onSurface
        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    },
    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .alpha(if (task.isDone) 0.5f else 1f),
    ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = onToggleDone,
                modifier = Modifier
                    .size(40.dp) // Adjust size as needed
                    .clip(CircleShape)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.name,
                    style = baseStyle
                )

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
       // }
    }
}