package com.wismna.geoffroy.donext.presentation.ui.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<UiEvent>(replay = 1)
    val events = _events.asSharedFlow()

    suspend fun send(event: UiEvent) {
        _events.emit(event)
    }
}
