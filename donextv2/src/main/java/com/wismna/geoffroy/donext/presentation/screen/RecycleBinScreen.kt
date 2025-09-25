package com.wismna.geoffroy.donext.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.presentation.viewmodel.RecycleBinViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskItemViewModel

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
    } else {
        val context = LocalContext.current
        LazyColumn(
            modifier = modifier.padding(8.dp)
        ) {
            items(tasks, key = { it.id!! }) { task ->

                TaskItemScreen(
                    modifier = Modifier.animateItem(),
                    viewModel = TaskItemViewModel(task),
                    onTaskClick = { onTaskClick(task) },
                    onSwipeLeft = {
                        viewModel.restore(task.id!!)
                        Toast.makeText(context, "Task restored", Toast.LENGTH_SHORT).show()
                    },
                    onSwipeRight = {
                        viewModel.deleteForever(task.id!!)
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    }
                )

            }
        }
    }
}
