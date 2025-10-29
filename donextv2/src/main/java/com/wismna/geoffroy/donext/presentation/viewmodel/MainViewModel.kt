package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getTaskListsUseCase: GetTaskListsUseCase,
    val uiEventBus: UiEventBus
) : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var destinations by mutableStateOf<List<AppDestination>>(emptyList())
        private set

    var startDestination by mutableStateOf<AppDestination>(AppDestination.ManageLists)
        private set

    var currentDestination by mutableStateOf(startDestination)
        private set

    var showTaskSheet by mutableStateOf(false)
    var showAddListSheet by mutableStateOf(false)

    init {
        getTaskListsUseCase()
            .onEach { lists ->
                destinations = lists.map { taskList ->
                    AppDestination.TaskList(taskList.id!!, taskList.name)
                } +
                    AppDestination.ManageLists +
                    AppDestination.RecycleBin +
                    AppDestination.DueTodayList
                if (startDestination == AppDestination.ManageLists && destinations.isNotEmpty()) {
                    startDestination = destinations.first()
                }
                isLoading = false
            }
            .launchIn(viewModelScope)
    }

    fun navigateBack() {
        viewModelScope.launch {
            uiEventBus.send(UiEvent.NavigateBack)
        }
    }

    fun onNewTaskButtonClicked(taskLisId: Long) {
        showTaskSheet = true
        viewModelScope.launch {
            uiEventBus.send(UiEvent.CreateNewTask(taskLisId))
        }
    }

    fun onDismissTaskSheet() {
        showTaskSheet = false
        viewModelScope.launch {
            uiEventBus.send(UiEvent.CloseTask)
            uiEventBus.clearSticky()
        }
    }

    fun setCurrentDestination(navBackStackEntry: NavBackStackEntry?) {
        val route = navBackStackEntry?.destination?.route
        val taskListId = navBackStackEntry?.arguments?.getLong("taskListId")

        currentDestination = destinations.firstOrNull { dest ->
            when (dest) {
                is AppDestination.TaskList -> taskListId != null && dest.taskListId == taskListId
                else -> dest.route == route
            }
        } ?: startDestination
    }

    fun doesListExist(taskListId: Long): Boolean {
        return destinations.any { dest ->
            dest is AppDestination.TaskList && dest.taskListId == taskListId
        }
    }
}