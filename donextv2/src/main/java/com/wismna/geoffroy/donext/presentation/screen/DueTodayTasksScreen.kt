package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.R
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
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Due today background icon",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.today_no_tasks), color = MaterialTheme.colorScheme.secondary)
            }
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
                    onSwipeRight = { viewModel.deleteTask(task.id!!) },
                    onTaskClick = { viewModel.onTaskClicked(task) }
                )
            }
        }
    }
}