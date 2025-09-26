package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.usecase.AddTaskListUseCase
import com.wismna.geoffroy.donext.domain.usecase.DeleteTaskListUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageListsViewModel @Inject constructor(
    getTaskListsUseCase: GetTaskListsUseCase,
    private val addTaskListUseCase: AddTaskListUseCase,
    private val updateTaskListUseCase: UpdateTaskListUseCase,
    private val deleteTaskListUseCase: DeleteTaskListUseCase
) : ViewModel() {

    var taskLists by mutableStateOf<List<TaskList>>(emptyList())
        private set
    var taskCount by mutableIntStateOf(0)
        private set

    init {
        getTaskListsUseCase()
            .onEach { lists ->
                taskLists = lists
                taskCount = lists.size
            }
            .launchIn(viewModelScope)
    }

    fun createTaskList(title: String, order: Int) {
        viewModelScope.launch {
            addTaskListUseCase(title, order)
        }
    }
    fun updateTaskListName(taskList: TaskList) {
        viewModelScope.launch {
            updateTaskListUseCase(taskList.id!!, taskList.name, taskList.order)
        }
    }
    fun deleteTaskList(taskId: Long) {
        viewModelScope.launch {
            deleteTaskListUseCase(taskId)
        }
    }

    fun moveTaskList(fromIndex: Int, toIndex: Int) {
        val mutable = taskLists.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        taskLists = mutable
    }

    fun commitTaskListOrder() {
        viewModelScope.launch {
            taskLists.forEachIndexed { index, list ->
                if (list.order != index) {
                    updateTaskListUseCase(list.id!!, list.name, index)
                }
            }
        }
    }
}