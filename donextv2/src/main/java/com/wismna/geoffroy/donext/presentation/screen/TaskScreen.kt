package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "New Task",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            // --- Title ---
            OutlinedTextField(
                value = viewModel.title,
                singleLine = true,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                isError = viewModel.title.isEmpty(),
            )
            Spacer(Modifier.height(8.dp))

            // --- Description ---
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // --- Priority ---
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButton(
                value = viewModel.priority,
                onValueChange = { viewModel.onPriorityChanged(it) }
            )
            Spacer(Modifier.height(16.dp))

            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // --- Delete Button ---
                if (viewModel.isEditing()) {
                    Button(
                        onClick = { viewModel.delete(); onDismiss() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Delete") }
                }
                // --- Save Button ---
                Button(
                    onClick = {
                        viewModel.save()
                        onDismiss()
                    },
                    enabled = viewModel.title.isNotBlank(),
                    //modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (viewModel.isEditing()) "Save" else "Create")
                }
            }
        }
    }
}