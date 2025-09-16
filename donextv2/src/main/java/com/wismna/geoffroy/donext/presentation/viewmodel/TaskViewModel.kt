package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.AddTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.DeleteTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
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

    private var editingTaskId: Long? = null
    private var taskListId: Long? = null

    fun isEditing(): Boolean = editingTaskId != null

    fun startNewTask(selectedListId: Long) {
        editingTaskId = null
        taskListId = selectedListId
        title = ""
        description = ""
        priority = Priority.NORMAL
        dueDate = null
    }

    fun startEditTask(task: Task) {
        editingTaskId = task.id
        taskListId = task.taskListId
        title = task.name
        description = task.description ?: ""
        priority = task.priority
        dueDate = task.dueDate
        isDone = task.isDone
    }

    fun onTitleChanged(value: String) { title = value }
    fun onDescriptionChanged(value: String) { description = value }
    fun onPriorityChanged(value: Priority) { priority = value }
    fun onDueDateChanged(value: Long?) { dueDate = value }

        fun save(onDone: (() -> Unit)? = null) {
        if (title.isBlank()) return

        viewModelScope.launch {
            if (isEditing()) {
                updateTaskUseCase(editingTaskId!!, taskListId!!, title, description, priority, dueDate, isDone)
            } else {
                createTaskUseCase(taskListId!!, title, description, priority, dueDate)
            }
            // reset state after save
            reset()
            onDone?.invoke()
        }
    }

    fun delete() {
        editingTaskId?.let { id ->
            viewModelScope.launch {
                deleteTaskUseCase(id)
                reset()
            }
        }
    }

    fun reset() {
        editingTaskId = null
        taskListId = null
        title = ""
        description = ""
        priority = Priority.NORMAL
    }
}