package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.extension.toLocalDate
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TaskItemViewModel @Inject constructor(
    private val uiEventBus: UiEventBus
): ViewModel() {
    var id: Long? = null
    var name: String? = null
    var description: String? = null
    var dueDate: String? = null
    var isDone: Boolean = false
    var isDeleted: Boolean = false
    var priority: Priority = Priority.NORMAL
    var isOverdue: Boolean = false
    val today: LocalDate = LocalDate.now(ZoneId.systemDefault())

    fun populateTask(task: Task) {
        id = task.id!!
        name = task.name
        description = task.description
        dueDate = task.dueDate?.let { formatDueDate(it) }
        isDone = task.isDone
        isDeleted = task.isDeleted
        priority = task.priority
        isOverdue = task.dueDate?.let { millis ->
            val dueDate = millis.toLocalDate()
            dueDate.isBefore(today)
        } ?: false
    }

    fun onTaskClicked(task: Task) {
        viewModelScope.launch {
            uiEventBus.send(UiEvent.EditTask(task))
        }
    }

    private fun formatDueDate(dueMillis: Long): String {
        val dueDateLocal = dueMillis.toLocalDate()

        return when {
            dueDateLocal.isEqual(today) -> "Today"
            dueDateLocal.isEqual(today.plusDays(1)) -> "Tomorrow"
            dueDateLocal.isEqual(today.minusDays(1)) -> "Yesterday"
            dueDateLocal.isAfter(today) && dueDateLocal.isBefore(today.plusDays(7)) ->
                dueDateLocal.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            else ->
                dueDateLocal.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
        }
    }

}