package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel

@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskListViewModel = hiltViewModel<TaskListViewModel>(),
) {
    val tasks = viewModel.tasks

    if (tasks.isEmpty()) {
        // Placeholder when recycle bin is empty
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Tap + to create a new task.")
        }
        return
    }

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
            TaskItemScreen(
                modifier = Modifier.animateItem(),
                task = task,
                onSwipeLeft = { viewModel.updateTaskDone(task.id!!, true) },
                onSwipeRight = { viewModel.deleteTask(task.id!!) },
                onTaskClick = { viewModel.onTaskClicked(task) }
            )
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
            TaskItemScreen(
                modifier = Modifier.animateItem(),
                task = task,
                onSwipeLeft = { viewModel.updateTaskDone(task.id!!, false) },
                onSwipeRight = { viewModel.deleteTask(task.id!!) },
                onTaskClick = { viewModel.onTaskClicked(task) }
            )
        }
    }
}