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
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val uiEventBus: UiEventBus
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var priority by mutableStateOf(Priority.NORMAL)
        private set
    var dueDate by mutableStateOf<Long?>(null)
        private set
    var isDone by mutableStateOf(false)
        private set
    var isDeleted by mutableStateOf(false)
        private set

    private var editingTaskId: Long? = null
    private var taskListId: Long? = null

    init {
        viewModelScope.launch {
            uiEventBus.events.collect { event ->
                when (event) {
                    is UiEvent.CreateNewTask -> startNewTask(event.taskListId)
                    is UiEvent.EditTask -> startEditTask(event.task)
                    is UiEvent.CloseTask -> reset()
                    else -> {}
                }
            }
        }
    }

    fun screenTitle(): String = if (isDeleted) "Task details" else if (isEditing()) "Edit Task" else "New Task"
    fun isEditing(): Boolean = editingTaskId != null
    fun onTitleChanged(value: String) { title = value }
    fun onDescriptionChanged(value: String) { description = value }
    fun onPriorityChanged(value: Priority) { priority = value }
    fun onDueDateChanged(value: Long?) {
        dueDate = if (value == null) null else
            Instant.ofEpochMilli(value)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
    }

    fun save(onDone: (() -> Unit)? = null) {
        if (title.isBlank()) return

        viewModelScope.launch {
            if (isEditing()) {
                updateTaskUseCase(editingTaskId!!, taskListId!!, title, description, priority, dueDate, isDone)
            } else {
                createTaskUseCase(taskListId!!, title, description, priority, dueDate)
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
        isDeleted = false
    }

    private fun startEditTask(task: Task) {
        editingTaskId = task.id
        taskListId = task.taskListId
        title = task.name
        description = task.description ?: ""
        priority = task.priority
        dueDate = task.dueDate
        isDone = task.isDone
        isDeleted = task.isDeleted
    }

    private fun reset() {
        editingTaskId = null
        taskListId = null
        title = ""
        description = ""
        priority = Priority.NORMAL
    }
}