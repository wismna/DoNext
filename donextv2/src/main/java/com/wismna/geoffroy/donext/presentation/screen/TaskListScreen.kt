package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel

@Composable
fun TaskListScreen(viewModel: TaskListViewModel = hiltViewModel()) {
    val tasks = viewModel.tasks

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(tasks) { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                }
                Column {
                    val textStyle = TextStyle(textDecoration = if (task.isDeleted) TextDecoration.LineThrough else TextDecoration.None)
                    Text(task.name, fontSize = 18.sp, color = Color.DarkGray, style = textStyle)
                    Text(task.description.orEmpty(), fontSize = 14.sp, color = Color.LightGray, style = textStyle, maxLines = 3)
                }
            }
        }
    }
}