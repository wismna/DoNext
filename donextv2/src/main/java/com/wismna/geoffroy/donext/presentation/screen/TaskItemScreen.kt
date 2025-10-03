package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Unpublished
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskItemViewModel

@Composable
fun TaskItemScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskItemViewModel,
    onTaskClick: (taskId: Long) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    // TODO: change this
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> { onSwipeRight() }
                SwipeToDismissBoxValue.EndToStart -> { onSwipeLeft() }
                SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState true
        },
        // positional threshold of 25%
        positionalThreshold = { it * .25f }
    )
    val baseStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = when (viewModel.priority) {
            Priority.HIGH -> FontWeight.Bold
            Priority.NORMAL -> FontWeight.Normal
            Priority.LOW -> FontWeight.Normal
        },
        color = when (viewModel.priority) {
            Priority.HIGH -> MaterialTheme.colorScheme.onSurface
            Priority.NORMAL -> MaterialTheme.colorScheme.onSurface
            Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        textDecoration = if (viewModel.isDone) TextDecoration.LineThrough else TextDecoration.None
    )
    Card(
        modifier = modifier,
        onClick = { onTaskClick(viewModel.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DismissBackground(
                    dismissState,
                    viewModel.isDone,
                    viewModel.isDeleted
                )
            },
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(8.dp)
                        .alpha(if (viewModel.isDone || viewModel.priority == Priority.LOW) 0.5f else 1f),
                    verticalAlignment = Alignment.CenterVertically // centers checkbox + content
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(IntrinsicSize.Min) // shrink to fit title/description
                    ) {
                        // Title
                        Text(
                            text = viewModel.name,
                            fontSize = 18.sp,
                            style = baseStyle,
                            modifier = Modifier
                                .align(
                                    if (viewModel.description.isNullOrBlank()) Alignment.CenterStart
                                    else Alignment.TopStart
                                ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )

                        // Due date badge
                        viewModel.dueDateText?.let { dueMillis ->
                            Badge(
                                modifier = Modifier
                                    .align(
                                        if (viewModel.description.isNullOrBlank()) Alignment.CenterEnd
                                        else Alignment.TopEnd
                                    ),
                                containerColor = if (viewModel.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = 1.dp, end = 1.dp),
                                    text = viewModel.dueDateText,
                                    color = if (viewModel.isOverdue) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Optional description
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (!viewModel.description.isNullOrBlank()) {
                                Text(
                                    text = viewModel.description,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = baseStyle.copy(
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            })
    }
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, isDone: Boolean, isDeleted: Boolean) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error
        SwipeToDismissBoxValue.EndToStart -> Color(0xFF18590D)
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isDeleted) Icons.Default.DeleteForever else Icons.Default.DeleteOutline,
                tint = Color.LightGray,
                contentDescription = "Delete"
            )
            Text(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                text = if (isDeleted) "Delete" else "Recycle"
            )
        }
        Spacer(modifier = Modifier)
        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isDeleted) Icons.Default.RestoreFromTrash else
                    if (isDone) Icons.Outlined.Unpublished else Icons.Outlined.CheckCircle,
                tint = Color.LightGray,
                contentDescription = "Archive"
            )
            Text(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 10.sp,
                text = if (isDeleted) "Restore" else if (isDone) "Undone" else "Done"
            )
        }
    }
}