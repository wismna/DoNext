package com.wismna.geoffroy.donext.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.presentation.viewmodel.RecycleBinViewModel

@Composable
fun RecycleBinScreen(
    modifier: Modifier = Modifier,
    viewModel: RecycleBinViewModel = hiltViewModel(),
    onTaskClick: (task: Task) -> Unit
) {
    val tasks = viewModel.deletedTasks

    if (tasks.isEmpty()) {
        // Placeholder when recycle bin is empty
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recycle Bin is empty")
        }
        return
    }

    val grouped = tasks.groupBy { it.listName }

    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.padding(8.dp)
    ) {
        // Deleted tasks are grouped by list name
        grouped.forEach { (listName, items) ->
            stickyHeader {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = listName,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                    )
                }
            }
            items(items, key = { it.task.id!! }) { item ->
                TaskItemScreen(
                    modifier = Modifier.animateItem(),
                    task = item.task,
                    onTaskClick = { onTaskClick(item.task) },
                    onSwipeLeft = {
                        viewModel.restore(item.task.id!!)
                        Toast.makeText(context, "Task restored", Toast.LENGTH_SHORT).show()
                    },
                    onSwipeRight = {
                        viewModel.deleteForever(item.task.id!!)
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyRecycleBinAction(viewModel: RecycleBinViewModel = hiltViewModel()) {
    val isEmpty = viewModel.deletedTasks.isEmpty()
    var showConfirmDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showConfirmDialog = true },
        enabled = !isEmpty) {
        Icon(
            Icons.Default.DeleteSweep,
            modifier = Modifier.alpha(if (isEmpty) 0.5f else 1.0f),
            contentDescription = "Empty Recycle Bin",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Empty Recycle Bin") },
            text = {
                Text("Are you sure you want to permanently delete all tasks in the recycle bin? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyRecycleBin()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
