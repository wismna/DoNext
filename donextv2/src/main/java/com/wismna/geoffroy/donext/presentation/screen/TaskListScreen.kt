package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    // Split tasks into active and done
    val (active, done) = remember(tasks) {
        tasks.partition { !it.isDone }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active tasks section
        items(
            items = active,
            key = { it.id!! }
        ) { task ->
            Card(
                onClick = { onTaskClick(task) },
                //elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                TaskItemScreen(
                    modifier = Modifier.animateItem(),
                    viewModel = TaskItemViewModel(task),
                    onSwipeDone = {
                        viewModel.updateTaskDone(task.id!!, true)
                    },
                    onSwipeDelete = {
                        viewModel.deleteTask(task.id!!)
                    }
                )
            }
        }

        // Divider between active and done (optional)
        if (done.isNotEmpty() && active.isNotEmpty()) {
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        // Done tasks section
        items(
            items = done,
            key = { it.id!! }
        ) { task ->
            Card(
                onClick = { onTaskClick(task) },
                //elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                TaskItemScreen(
                    modifier = Modifier.animateItem(),
                    viewModel = TaskItemViewModel(task),
                    onSwipeDone = {
                        viewModel.updateTaskDone(task.id!!, false)
                    },
                    onSwipeDelete = {
                        viewModel.deleteTask(task.id!!)
                    },
                )
            }
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