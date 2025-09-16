package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskItemViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskListViewModel = hiltViewModel<TaskListViewModel>(),
    onTaskClick: (Task) -> Unit) {
    val tasks = viewModel.tasks

    LazyColumn(
        modifier = modifier.fillMaxSize().padding()
    ) {
        itemsIndexed(tasks, key = { id, task -> task.id!! }) { index, task ->
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

            TaskItemScreen(
                modifier = Modifier.animateItem(),
                viewModel = TaskItemViewModel(task),
                onClick = { onTaskClick(task) },
                onToggleDone = { isChecked ->
                    viewModel.updateTaskDone(task.id!!, isChecked)
                }
            )
        }
    }
}

@Composable
fun TaskListFab(
    taskListId: Long,
    viewModel: TaskViewModel = hiltViewModel(),
    showBottomSheet: (Boolean) -> Unit = {}
) {
    ExtendedFloatingActionButton(
        onClick = {
            viewModel.startNewTask(taskListId)
            showBottomSheet(true)
        },
        icon = { Icon(Icons.Filled.Add, "Create a task.") },
        text = { Text("Create a task") },
    )
}