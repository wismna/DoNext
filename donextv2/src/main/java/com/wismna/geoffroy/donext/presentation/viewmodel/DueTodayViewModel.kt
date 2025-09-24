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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DueTodayViewModel @Inject constructor(
    getDueTodayTasks: GetDueTodayTasksUseCase,
    private val toggleTaskDeleted: ToggleTaskDeletedUseCase,
    private val toggleTaskDone: ToggleTaskDoneUseCase
) : ViewModel() {

    var dueTodayTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        getDueTodayTasks()
            .onEach { tasks ->
                dueTodayTasks = tasks
            }
            .launchIn(viewModelScope)
    }

    fun updateTaskDone(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDone(taskId, true)
        }
    }
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDeleted(taskId, true)
        }
    }
}