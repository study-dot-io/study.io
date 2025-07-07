package com.example.studyio.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object QuizEvents {
    private val _quizCompleted = MutableSharedFlow<Unit>()
    val quizCompleted = _quizCompleted.asSharedFlow()

    suspend fun decksUpdated() {
        _quizCompleted.emit(Unit)
    }
}

