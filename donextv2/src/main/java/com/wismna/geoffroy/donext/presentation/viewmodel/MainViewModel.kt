package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getTaskLists: GetTaskListsUseCase
) : ViewModel() {

    var taskLists by mutableStateOf<List<TaskList>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    init {
        getTaskLists()
            .onEach { lists ->
                taskLists = lists
                isLoading = false
            }
            .launchIn(viewModelScope)
    }
}