package com.example.studyio.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object Events {
    private val _deckUpdated = MutableSharedFlow<Unit>()
    private val _quizCompleted = MutableSharedFlow<Unit>()
    val deckUpdated = _deckUpdated.asSharedFlow()
    val quizCompleted = _quizCompleted.asSharedFlow()

    suspend fun decksUpdated() {
        _deckUpdated.emit(Unit)
    }
    
    suspend fun quizCompleted() {
        _quizCompleted.emit(Unit)
    }
}

