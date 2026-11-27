package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.AddTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val uiEventBus: UiEventBus,
    private val clock: Clock
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var priority by mutableStateOf(Priority.NORMAL)
        private set
    var dueDate by mutableStateOf<LocalDate?>(null)
        private set
    var dueTime by mutableStateOf<LocalTime?>(null)
        private set
    var isDone by mutableStateOf(false)
        private set
    var isDeleted by mutableStateOf(false)
        private set
    var taskListId by mutableStateOf<Long?>(null)
        private set
    var editingTaskId by mutableStateOf<Long?>(null)
        private set

    init {
        viewModelScope.launch {
            uiEventBus.stickyEvents.collect { event ->
                when (event) {
                    is UiEvent.CreateNewTask -> startNewTask(event.taskListId)
                    is UiEvent.EditTask -> startEditTask(event.task)
                    is UiEvent.CloseTask -> reset()
                    else -> {}
                }
            }
        }
    }

    fun isEditing(): Boolean = editingTaskId != null
    fun onTitleChanged(value: String) { title = value }
    fun onDescriptionChanged(value: String) { description = value }
    fun onPriorityChanged(value: Priority) { priority = value }
    fun onDueTimeChanged(value: LocalTime?) {
        if (value == null || isDueTimeAllowed(value)) dueTime = value
    }

    /** A time that already passed today would never notify, so it cannot be picked for a task due today. */
    fun isDueTimeAllowed(time: LocalTime): Boolean =
        dueDate != LocalDate.now(clock) || time.isAfter(LocalTime.now(clock))

    fun suggestedDueTime(): LocalTime {
        dueTime?.let { return it }
        if (dueDate != LocalDate.now(clock)) return DEFAULT_DUE_TIME
        val nextFullHour = LocalTime.now(clock).plusHours(1).withMinute(0).withSecond(0).withNano(0)
        return if (isDueTimeAllowed(nextFullHour)) nextFullHour else LocalTime.MAX.withSecond(0).withNano(0)
    }

    /** [utcMillis] is the date picker's selection: midnight UTC of the picked day. */
    fun onDueDateChanged(utcMillis: Long?) {
        if (utcMillis == null) dueTime = null
        dueDate = utcMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
        dueTime = dueTime?.takeIf { isDueTimeAllowed(it) }
    }

    fun save(onDone: (() -> Unit)? = null) {
        if (title.isBlank()) return
        val listId = taskListId ?: return

        viewModelScope.launch {
            val currentEditingTaskId = editingTaskId
            if (currentEditingTaskId != null) {
                updateTaskUseCase(currentEditingTaskId, listId, title, description, priority, dueDate, dueTime)
            } else {
                createTaskUseCase(listId, title, description, priority, dueDate, dueTime)
            }
            onDone?.invoke()
        }
    }

    private fun startNewTask(selectedListId: Long) {
        editingTaskId = null
        taskListId = selectedListId
        title = ""
        description = ""
        priority = Priority.NORMAL
        dueDate = null
        dueTime = null
        isDeleted = false
    }

    private fun startEditTask(task: Task) {
        editingTaskId = task.id
        taskListId = task.taskListId
        title = task.name
        description = task.description ?: ""
        priority = task.priority
        dueDate = task.dueDate
        dueTime = task.dueTime
        isDone = task.isDone
        isDeleted = task.isDeleted
    }

    private fun reset() {
        editingTaskId = null
        taskListId = null
        title = ""
        description = ""
        priority = Priority.NORMAL
        dueDate = null
        dueTime = null
        isDone = false
        isDeleted = false
    }

    private companion object {
        val DEFAULT_DUE_TIME: LocalTime = LocalTime.of(9, 0)
    }
}