package com.wismna.geoffroy.donext.presentation.screen

import android.text.format.DateFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.wismna.geoffroy.donext.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    isTimeAllowed: (LocalTime) -> Boolean = { true }
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
            TextButton(onClick = { onConfirm(pickedTime) }, enabled = isTimeAllowed(pickedTime)) {
                Text(stringResource(R.string.dialog_due_date_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_due_date_cancel))
            }
        },
        text = { TimePicker(state = timePickerState) }
    )
}
