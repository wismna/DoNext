package com.wismna.geoffroy.donext.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.Task
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

    val context = LocalContext.current
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
                onTaskClick = { onTaskClick(task) },
                onSwipeLeft = {
                    viewModel.updateTaskDone(task.id!!, true)
                    Toast.makeText(context, "Task done", Toast.LENGTH_SHORT).show()
                },
                onSwipeRight = {
                    viewModel.deleteTask(task.id!!)
                    Toast.makeText(context, "Task moved to recycle bin", Toast.LENGTH_SHORT).show()
                }
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
                onTaskClick = { onTaskClick(task) },
                onSwipeLeft = {
                    viewModel.updateTaskDone(task.id!!, false)
                    Toast.makeText(context, "Task in progress", Toast.LENGTH_SHORT).show()
                },
                onSwipeRight = {
                    viewModel.deleteTask(task.id!!)
                    Toast.makeText(context, "Task moved to recycle bin", Toast.LENGTH_SHORT).show()
                },
            )
        }

    }
}