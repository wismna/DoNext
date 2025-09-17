package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        getTaskLists()
            .onEach { lists ->
                destinations = lists.map { taskList ->
                    AppDestination.TaskList(taskList.id!!, taskList.name)
                } + AppDestination.ManageLists
                isLoading = false
            }
            .launchIn(viewModelScope)
    }

    fun deriveDestination(route: String?): AppDestination? {
        if (route == null) return null
        return destinations.firstOrNull { dest ->
            when (dest) {
                is AppDestination.TaskList -> route.startsWith("tasklist/")
                else -> dest.route == route
            }
        }
    }
}