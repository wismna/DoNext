package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.usecase.GetDeletedTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.PermanentlyDeleteTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.ToggleTaskDeletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val getDeletedTasks: GetDeletedTasksUseCase,
    private val restoreTask: ToggleTaskDeletedUseCase,
    private val permanentlyDeleteTask: PermanentlyDeleteTaskUseCase
) : ViewModel() {

    var deletedTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        loadDeletedTasks()
    }

    fun loadDeletedTasks() {
        viewModelScope.launch {
            deletedTasks = getDeletedTasks()
        }
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
}
