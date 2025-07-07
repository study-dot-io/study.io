package com.example.studyio.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.QuizRepository
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.Note
import com.example.studyio.events.Events
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val _dueCards = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards
    private val _notesById = MutableStateFlow<Map<Long, Note>>(emptyMap())
    val notesById: StateFlow<Map<Long, Note>> = _notesById
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex
    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete
    private val _quizCompletedEvent = MutableSharedFlow<Unit>()
    val quizCompletedEvent: SharedFlow<Unit> = _quizCompletedEvent

    fun loadQuiz(deckId: Long) {
        viewModelScope.launch {
            val cards = quizRepository.getDueCards(deckId, 200) // Limit the number of cards loaded
            _dueCards.value = cards
            val noteIds = cards.map { it.noteId }
            _notesById.value = quizRepository.getNotesByIds(noteIds)
            _currentIndex.value = 0
            _isComplete.value = cards.isEmpty()
        }
    }

    fun rateCard(rating: Int) {
        val cards = _dueCards.value
        val idx = _currentIndex.value
        if (idx >= cards.size) return
        val card = cards[idx]
        viewModelScope.launch {
            quizRepository.updateCard(
                card.copy(
                    due = (System.currentTimeMillis() / 1000).toInt() + (rating * 24 * 60 * 60)
                )
            )
            // Fire event after every card is answered
            Events.decksUpdated()
            val nextIndex = idx + 1
            if (nextIndex >= cards.size) {
                _isComplete.value = true
            } else {
                _currentIndex.value = nextIndex
            }
        }
    }
}
