package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.DeleteTaskListUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTasksForListUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    getTasks: GetTasksForListUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val toggleTaskDeletedUseCase: ToggleTaskDeletedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    private val taskListId: Long = checkNotNull(savedStateHandle["taskListId"])

    init {
        getTasks(taskListId)
            .onEach { list ->
                tasks = list
                isLoading = false
            }
            .launchIn(viewModelScope)
    }

    fun updateTaskDone(taskId: Long, isDone: Boolean) {
        viewModelScope.launch {
            toggleTaskDoneUseCase(taskId, isDone)
        }
    }
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            toggleTaskDeletedUseCase(taskId, true)
        }
    }
}