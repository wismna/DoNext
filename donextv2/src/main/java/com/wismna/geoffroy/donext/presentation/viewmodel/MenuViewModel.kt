package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsWithOverdueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    getTaskListsWithOverdue: GetTaskListsWithOverdueUseCase
) : ViewModel() {

    var taskLists by mutableStateOf<List<TaskListWithOverdue>>(emptyList())
        private set

    init {
        getTaskListsWithOverdue()
            .onEach { lists ->
                taskLists = lists
            }
            .launchIn(viewModelScope)
    }
}
