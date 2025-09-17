package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskItemViewModel

@Composable
fun TaskItemScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskItemViewModel,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit
) {
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
        textDecoration = if (viewModel.isDone) TextDecoration.LineThrough else TextDecoration.None)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .alpha(if (viewModel.isDone || viewModel.priority == Priority.LOW) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically // centers checkbox + content
    ) {
        // Done checkbox
        Checkbox(
            checked = viewModel.isDone,
            onCheckedChange = onToggleDone,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .height(IntrinsicSize.Min) // shrink to fit title/description
        ) {
            // Title
            Text(
                text = viewModel.name,
                style = baseStyle,
                modifier = Modifier
                    .align(
                        if (viewModel.description.isNullOrBlank()) Alignment.CenterStart
                        else Alignment.TopStart
                    )
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
            this@Row.AnimatedVisibility(
                visible = !viewModel.description.isNullOrBlank(),
                modifier = Modifier.align(Alignment.BottomStart),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = viewModel.description!!,
                    style = baseStyle.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(top = 20.dp) // spacing below title
                )
            }

        }
    }
}