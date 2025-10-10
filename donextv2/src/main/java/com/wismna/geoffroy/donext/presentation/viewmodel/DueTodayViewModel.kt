package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.GetDueTodayTasksUseCase
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
class DueTodayViewModel @Inject constructor(
    getDueTodayTasksUseCase: GetDueTodayTasksUseCase,
    private val toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val uiEventBus: UiEventBus
) : ViewModel() {

    var dueTodayTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        getDueTodayTasksUseCase()
            .onEach { tasks ->
                dueTodayTasks = tasks
            }
            .launchIn(viewModelScope)
    }

    fun updateTaskDone(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDoneUseCase(taskId, true)

            uiEventBus.send(
                UiEvent.ShowUndoSnackbar(
                    message = "Task done",
                    undoAction = {
                        viewModelScope.launch {
                            toggleTaskDoneUseCase(taskId, false)
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
                    message = "Task moved to recycle bin",
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