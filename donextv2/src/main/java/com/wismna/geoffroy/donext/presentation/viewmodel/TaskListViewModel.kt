package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.GetTasksForListUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDoneUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getTasksUseCase: GetTasksForListUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase,
    private val uiEventBus: UiEventBus
) : ViewModel() {

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    private val taskListId: Long = checkNotNull(savedStateHandle["taskListId"])

    init {
        getTasksUseCase(taskListId)
            .onEach { list ->
                tasks = list
                isLoading = false
            }
            .launchIn(viewModelScope)
    }

    fun onTaskClicked(task: Task) {
        viewModelScope.launch {
            uiEventBus.send(UiEvent.EditTask(task))
        }
    }

    fun updateTaskDone(taskId: Long, isDone: Boolean) {
        viewModelScope.launch {
            toggleTaskDoneUseCase(taskId, isDone)

            uiEventBus.send(
                UiEvent.ShowUndoSnackbar(
                    message = if (isDone) R.string.snackbar_message_task_done else R.string.snackbar_message_task_undone,
                    undoAction = {
                        viewModelScope.launch {
                            toggleTaskDoneUseCase(taskId, !isDone)
                        }
                    }
                )
            )
        }
    }
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDeletedUseCase(taskId, true)

            uiEventBus.send(
                UiEvent.ShowUndoSnackbar(
                    message = R.string.snackbar_message_task_recycle,
                    undoAction = {
                        viewModelScope.launch {
                            toggleTaskDeletedUseCase(taskId, false)
                        }
                    }
                )
            )
        }
    }
}