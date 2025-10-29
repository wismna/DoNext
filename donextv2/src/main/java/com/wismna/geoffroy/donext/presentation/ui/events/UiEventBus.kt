package com.wismna.geoffroy.donext.presentation.ui.events

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiEventBus @Inject constructor() {
    // Non-replayable (e.g. navigation, snackbar)
    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    // Replayable (e.g. edit/create task)
    private val _stickyEvents = MutableSharedFlow<UiEvent>(replay = 1, extraBufferCapacity = 1)
    val stickyEvents = _stickyEvents.asSharedFlow()

    suspend fun send(event: UiEvent) {
        when (event) {
            is UiEvent.EditTask,
            is UiEvent.CreateNewTask -> _stickyEvents.emit(event)
            else -> _events.emit(event)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearSticky() {
        _stickyEvents.resetReplayCache()
    }
}
