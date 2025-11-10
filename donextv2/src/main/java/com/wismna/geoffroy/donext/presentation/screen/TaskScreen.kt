package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.extension.toLocalDate
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(
            stringResource(
                if (viewModel.isDeleted) R.string.task_title_deleted
                else
                    if (viewModel.isEditing()) R.string.task_title_edit
                    else R.string.task_title_new),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))

        // --- Title ---
        OutlinedTextField(
            value = viewModel.title,
            singleLine = true,
            readOnly = viewModel.isDeleted,
            onValueChange = { viewModel.onTitleChanged(it) },
            label = { Text(stringResource(R.string.task_name)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester)
        )
        Spacer(Modifier.height(12.dp))

        // --- Description ---
        OutlinedTextField(
            value = viewModel.description,
            readOnly = viewModel.isDeleted,
            onValueChange = { viewModel.onDescriptionChanged(it) },
            label = { Text(stringResource(R.string.task_description)) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // --- Priority ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButton(
                value = viewModel.priority,
                isEnabled = !viewModel.isDeleted,
                onValueChange = { viewModel.onPriorityChanged(it) }
            )
        }
        Spacer(Modifier.height(12.dp))

        // --- Due Date ---
        var showDatePicker by remember { mutableStateOf(false) }
        val formattedDate = viewModel.dueDate?.toLocalDate()?.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        )
            ?: ""

        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.task_due_date)) },
            trailingIcon = {
                Row {
                    if (viewModel.dueDate != null) {
                        IconButton(
                            onClick = { viewModel.onDueDateChanged(null) },
                            enabled = !viewModel.isDeleted
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear due date")
                        }
                    }
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = !viewModel.isDeleted
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick due date")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = viewModel.dueDate,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val todayStartUtcMillis = LocalDate.now(ZoneId.systemDefault())
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                            .toEpochMilli()
                        return utcTimeMillis >= todayStartUtcMillis
                    }
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.onDueDateChanged(it) }
                            showDatePicker = false
                        }) { Text(stringResource(R.string.dialog_due_date_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.dialog_due_date_cancel)) }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        if (!viewModel.isDeleted) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Cancel Button ---
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Cancel") }

                // --- Save Button ---
                Button(
                    onClick = {
                        viewModel.save()
                        onDismiss()
                    },
                    enabled = viewModel.title.isNotBlank() && !viewModel.isDeleted,
                ) {
                    Text(stringResource(if (viewModel.isEditing()) R.string.task_save_edit else R.string.task_save_new))
                }
            }
        }
    }
}

@Composable
fun SingleChoiceSegmentedButton(
    value: Priority,
    isEnabled: Boolean,
    onValueChange: (Priority) -> Unit) {
    val options = listOf(Priority.LOW.label, Priority.NORMAL.label, Priority.HIGH.label)

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                enabled = isEnabled,
                onClick = { onValueChange(Priority.fromValue(index)) },
                selected = index == value.value,
                label = { Text(stringResource(label)) }
            )
        }
    }
}