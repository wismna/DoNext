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

sealed class AppDestination(
    val route: String,
    val title: String,
    val showFab: Boolean = false,
    val showBackButton: Boolean = false
) {
    data class TaskList(val taskListId: Long, val name: String) : AppDestination(
        route = "taskList/$taskListId",
        title = name,
        showFab = true,
        showBackButton = false
    )

    object ManageLists : AppDestination(
        route = "manageLists",
        title = "Manage Lists",
        showFab = false,
        showBackButton = true
    )
}

@HiltViewModel
class MainViewModel @Inject constructor(
    getTaskListsWithOverdue: GetTaskListsWithOverdueUseCase
) : ViewModel() {

    var taskLists by mutableStateOf<List<TaskListWithOverdue>>(emptyList())
        private set
    val destinations: List<AppDestination>
        get() = taskLists.map { AppDestination.TaskList(it.id, it.name) } +
                AppDestination.ManageLists
    var isLoading by mutableStateOf(true)
        private set

    init {
        getTaskListsWithOverdue()
            .onEach { lists ->
                taskLists = lists
                isLoading = false
            }
            .launchIn(viewModelScope)
    }
}