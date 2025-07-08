package com.example.studyio.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardRepository
import com.example.studyio.events.Events
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    private val _dueCards = MutableStateFlow<List<Card>>(emptyList())
    val dueCards: StateFlow<List<Card>> = _dueCards
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex
    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete

    fun loadQuiz(deckId: String) {
        viewModelScope.launch {
            val cards = cardRepository.getDueCards(deckId, 200)
            _dueCards.value = cards
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
            cardRepository.updateCard(
                card.copy(
                    due = System.currentTimeMillis() / 1000 + (rating * 24 * 60 * 60)
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
