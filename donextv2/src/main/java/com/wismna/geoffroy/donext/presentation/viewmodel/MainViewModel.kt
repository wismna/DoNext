package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getTaskLists: GetTaskListsUseCase
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
        getTaskLists()
            .onEach { lists ->
                destinations = lists.map { taskList ->
                    AppDestination.TaskList(taskList.id!!, taskList.name)
                } + AppDestination.ManageLists
                isLoading = false
                if (!destinations.isEmpty()) startDestination = destinations.first()
            }
            .launchIn(viewModelScope)
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
}