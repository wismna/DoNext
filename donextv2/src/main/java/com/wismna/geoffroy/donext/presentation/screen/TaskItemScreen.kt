package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Badge
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
import androidx.compose.ui.text.font.FontStyle
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
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
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

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { DismissBackground(dismissState, viewModel.isDone) },
        content = {
            Row(
                modifier = modifier
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
                                    fontStyle = FontStyle.Italic
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

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState, isDone: Boolean) {
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
        Icon(
            Icons.Default.Delete,
            tint = Color.LightGray,
            contentDescription = "Delete"
        )
        Spacer(modifier = Modifier)
        Icon(
            if (isDone) Icons.Default.Close else Icons.Default.Done,
            tint = Color.LightGray,
            contentDescription = "Archive"
        )
    }
}