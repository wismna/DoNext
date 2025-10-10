package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val uiEventBus: UiEventBus
) : ViewModel() {

    var deletedTasks by mutableStateOf<List<TaskWithListName>>(emptyList())
        private set

    init {
        loadDeletedTasks()
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
            loadDeletedTasks()

            uiEventBus.send(
                UiEvent.ShowUndoSnackbar(
                    message = "Task restored",
                    undoAction = {
                        viewModelScope.launch {
                            toggleTaskDeletedUseCase(taskId, true)
                        }
                    }
                )
            )
        }
    }

    fun deleteForever(taskId: Long) {
        viewModelScope.launch {
            permanentlyDeleteTaskUseCase(taskId)
            loadDeletedTasks()
        }
    }
    fun emptyRecycleBin() {
        viewModelScope.launch {
            emptyRecycleBinUseCase()
        }
    }
}