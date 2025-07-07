package com.example.studyio.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object Events {
    private val _deckUpdated = MutableSharedFlow<Unit>()
    val deckUpdated = _deckUpdated.asSharedFlow()

    suspend fun decksUpdated() {
        _deckUpdated.emit(Unit)
    }
}

