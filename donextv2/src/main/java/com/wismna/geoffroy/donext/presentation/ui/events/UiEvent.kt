package com.wismna.geoffroy.donext.presentation.ui.events

import com.wismna.geoffroy.donext.domain.model.Task

sealed class UiEvent {
    data class Navigate(val route: String) : UiEvent()
    data object NavigateBack : UiEvent()
    data class EditTask(val task: Task) : UiEvent()
    data class CreateNewTask(val taskListId: Long) : UiEvent()
    data object CloseTask : UiEvent()
    data class ShowUndoSnackbar(
        val message: Int,
        val undoAction: () -> Unit
    ) : UiEvent()
}