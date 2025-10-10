package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.domain.usecase.GetDueTodayTasksUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsWithOverdueUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    getTaskListsWithOverdue: GetTaskListsWithOverdueUseCase,
    getDueTodayTasks: GetDueTodayTasksUseCase,
    private val uiEventBus: UiEventBus
) : ViewModel() {
    var taskLists by mutableStateOf<List<TaskListWithOverdue>>(emptyList())
        private set

    var dueTodayTasksCount by mutableIntStateOf(0)
        private set

    init {
        getTaskListsWithOverdue()
            .onEach { lists ->
                taskLists = lists
            }
            .launchIn(viewModelScope)
        getDueTodayTasks()
            .onEach { tasks ->
                dueTodayTasksCount = tasks.count()
            }
            .launchIn(viewModelScope)
    }

    fun navigateTo(route: String) {
        viewModelScope.launch {
            uiEventBus.send(UiEvent.Navigate(route))
        }
    }
}
