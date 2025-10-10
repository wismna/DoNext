package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.DueTodayViewModel

@Composable
fun DueTodayTasksScreen(
    modifier: Modifier = Modifier,
    viewModel: DueTodayViewModel = hiltViewModel(),
) {
    val tasks = viewModel.dueTodayTasks

    if (tasks.isEmpty()) {
        // Placeholder when recycle bin is empty
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nothing due today !")
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(8.dp)
        ) {
            items(tasks, key = { it.id!! }) { task ->
                TaskItemScreen(
                    modifier = Modifier.animateItem(),
                    task = task,
                    onSwipeLeft = { viewModel.updateTaskDone(task.id!!) },
                    onSwipeRight = { viewModel.deleteTask(task.id!!) }
                )
            }
        }
    }
}