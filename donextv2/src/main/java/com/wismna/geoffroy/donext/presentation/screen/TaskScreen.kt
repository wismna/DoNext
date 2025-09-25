package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
                if (viewModel.isEditing()) "Edit Task" else "New Task",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            // --- Title ---
            OutlinedTextField(
                value = viewModel.title,
                singleLine = true,
                readOnly = viewModel.isDone,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                isError = viewModel.title.isEmpty(),
            )
            Spacer(Modifier.height(12.dp))

            // --- Description ---
            OutlinedTextField(
                value = viewModel.description,
                readOnly = viewModel.isDone,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // --- Priority ---
            Row (
                modifier = Modifier.fillMaxWidth().padding(start = 17.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButton(
                    value = viewModel.priority,
                    onValueChange = { viewModel.onPriorityChanged(it) }
                )
            }
            Spacer(Modifier.height(12.dp))

            // --- Due Date ---
            var showDatePicker by remember { mutableStateOf(false) }
            val formattedDate = viewModel.dueDate?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            } ?: ""

            OutlinedTextField(
                value = formattedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date") },
                trailingIcon = {
                    Row {
                        if (viewModel.dueDate != null) {
                            IconButton(
                                onClick = { viewModel.onDueDateChanged(null) },
                                enabled = !viewModel.isDone) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear due date")
                            }
                        }
                        IconButton(
                            onClick = { showDatePicker = true },
                            enabled = !viewModel.isDone) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick due date")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = viewModel.dueDate,
                    selectableDates = object: SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val todayStartMillis = LocalDate.now(ZoneOffset.UTC)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant()
                                .toEpochMilli()
                            return utcTimeMillis >= todayStartMillis
                        }
                    }
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.onDueDateChanged(it) }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (viewModel.isEditing()) Arrangement.SpaceBetween else Arrangement.End) {

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
                    enabled = viewModel.title.isNotBlank() && !viewModel.isDone,
                ) {
                    Text(if (viewModel.isEditing()) "Save" else "Create")
                }
            }
        }
    }
}

@Composable
fun SingleChoiceSegmentedButton(
    value: Priority,
    onValueChange: (Priority) -> Unit) {
    val options = listOf(Priority.LOW.label, Priority.NORMAL.label, Priority.HIGH.label)

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onValueChange(Priority.fromValue(index)) },
                selected = index == value.value,
                label = { Text(label) }
            )
        }
    }
}