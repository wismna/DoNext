package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskWithListName
import com.wismna.geoffroy.donext.domain.usecase.EmptyRecycleBinUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetDeletedTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.PermanentlyDeleteTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val getDeletedTasksUseCase: GetDeletedTasksUseCase,
    private val toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase,
    private val permanentlyDeleteTaskUseCase: PermanentlyDeleteTaskUseCase,
    private val emptyRecycleBinUseCase: EmptyRecycleBinUseCase,
    private val uiEventBus: UiEventBus,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TASK_TO_DELETE = "taskToDeleteId"
        private const val EMPTY_RECYCLE_BIN = "emptyRecycleBin"
    }
    var deletedTasks by mutableStateOf<List<TaskWithListName>>(emptyList())
        private set

    val taskToDeleteFlow = savedStateHandle.getStateFlow<Long?>(TASK_TO_DELETE, null)
    val emptyRecycleBinFlow = savedStateHandle.getStateFlow<Boolean>(EMPTY_RECYCLE_BIN, false)

    init {
        loadDeletedTasks()
    }

    fun onTaskClicked(task: Task) {
        viewModelScope.launch {
            uiEventBus.send(UiEvent.EditTask(task))
        }
    }

    fun loadDeletedTasks() {
        getDeletedTasksUseCase()
            .onEach { tasks ->
                deletedTasks = tasks
            }
            .launchIn(viewModelScope)
    }

    fun restore(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDeletedUseCase(taskId, false)

            uiEventBus.send(
                UiEvent.ShowUndoSnackbar(
                    message = R.string.snackbar_message_task_restore,
                    undoAction = {
                        viewModelScope.launch {
                            toggleTaskDeletedUseCase(taskId, true)
                        }
                    }
                )
            )
        }
    }

    fun onEmptyRecycleBinRequest() {
        savedStateHandle[EMPTY_RECYCLE_BIN] = true
    }

    fun onCancelEmptyRecycleBinRequest() {
        savedStateHandle[EMPTY_RECYCLE_BIN] = false
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            emptyRecycleBinUseCase()
            savedStateHandle[EMPTY_RECYCLE_BIN] = false
        }
    }

    fun onTaskDeleteRequest(taskId: Long) {
        savedStateHandle[TASK_TO_DELETE] = taskId
    }

    fun onConfirmDelete() {
        taskToDeleteFlow.value?.let {
            viewModelScope.launch {
                permanentlyDeleteTaskUseCase(it)
            }
        }
        savedStateHandle[TASK_TO_DELETE] = null
    }

    fun onCancelDelete() {
        savedStateHandle[TASK_TO_DELETE] = null
    }
}