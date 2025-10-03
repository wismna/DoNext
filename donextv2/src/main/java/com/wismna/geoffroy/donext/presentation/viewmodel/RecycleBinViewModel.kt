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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val getDeletedTasks: GetDeletedTasksUseCase,
    private val restoreTask: ToggleTaskDeletedUseCase,
    private val permanentlyDeleteTask: PermanentlyDeleteTaskUseCase,
    private val emptyRecycleBinUseCase: EmptyRecycleBinUseCase
) : ViewModel() {

    var deletedTasks by mutableStateOf<List<TaskWithListName>>(emptyList())
        private set

    init {
        loadDeletedTasks()
    }

    fun loadDeletedTasks() {
        getDeletedTasks()
            .onEach { tasks ->
                deletedTasks = tasks
            }
            .launchIn(viewModelScope)
    }

    fun restore(taskId: Long) {
        viewModelScope.launch {
            restoreTask(taskId, false)
            loadDeletedTasks()
        }
    }

    fun deleteForever(taskId: Long) {
        viewModelScope.launch {
            permanentlyDeleteTask(taskId)
            loadDeletedTasks()
        }
    }
    fun emptyRecycleBin() {
        viewModelScope.launch {
            emptyRecycleBinUseCase()
        }
    }
}