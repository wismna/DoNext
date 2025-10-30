package com.wismna.geoffroy.donext.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.presentation.viewmodel.RecycleBinViewModel

@Composable
fun RecycleBinScreen(
    modifier: Modifier = Modifier,
    viewModel: RecycleBinViewModel = hiltViewModel(),
) {
    val tasks = viewModel.deletedTasks
    val taskToDelete by viewModel.taskToDeleteFlow.collectAsStateWithLifecycle()

    if (tasks.isEmpty()) {
        // Placeholder when recycle bin is empty
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Recycle bin background icon",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(stringResource(R.string.recycle_bin_no_tasks), color = MaterialTheme.colorScheme.secondary)
            }
        }
        return
    }

    val grouped = tasks.groupBy { it.listName }
    val context = LocalContext.current

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelDelete() },
            title = { Text(stringResource(R.string.dialog_delete_task_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_task_description))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onConfirmDelete()
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.dialog_delete_task_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelDelete() }) {
                    Text(stringResource(R.string.dialog_delete_task_cancel))
                }
            }
        )
    }
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
                    onSwipeLeft = { viewModel.restore(item.task.id!!) },
                    onSwipeRight = { viewModel.onTaskDeleteRequest(item.task.id!!) },
                    onTaskClick = { viewModel.onTaskClicked(item.task) }
                )
            }
        }
    }
}

@Composable
fun EmptyRecycleBinAction(viewModel: RecycleBinViewModel = hiltViewModel()) {
    val isEmpty = viewModel.deletedTasks.isEmpty()
    val emptyRecycleBin by viewModel.emptyRecycleBinFlow.collectAsStateWithLifecycle()

    IconButton(
        onClick = { viewModel.onEmptyRecycleBinRequest() },
        enabled = !isEmpty) {
        Icon(
            Icons.Default.DeleteSweep,
            modifier = Modifier.alpha(if (isEmpty) 0.5f else 1.0f),
            contentDescription = stringResource(R.string.dialog_empty_task_title),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    if (emptyRecycleBin) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelEmptyRecycleBinRequest() },
            title = { Text(stringResource(R.string.dialog_empty_task_title)) },
            text = {
                Text(stringResource(R.string.dialog_empty_task_description))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyRecycleBin()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.dialog_empty_task_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelEmptyRecycleBinRequest() }) {
                    Text(stringResource(R.string.dialog_empty_task_cancel))
                }
            }
        )
    }
}
