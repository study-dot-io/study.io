package com.example.studyio.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.FSRScheduler
import com.example.studyio.data.QuizRepository
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private val _cramMode = MutableStateFlow(false)
    val cramMode: StateFlow<Boolean> = _cramMode

    fun loadQuiz(deckId: Long) {
        viewModelScope.launch {
            val todayEpoch = LocalDate.now().toEpochDay().toInt()
            val cards = quizRepository.getCardsDueToday(deckId, todayEpoch, 200)
            if (cards.isNotEmpty()) {
                _dueCards.value = cards
                _cramMode.value = false
            } else {
                val allCards = quizRepository.getAllCardsOrderedByDue(deckId)
                _dueCards.value = allCards
                _cramMode.value = true
            }
            val noteIds = _dueCards.value.map { it.noteId }
            _notesById.value = quizRepository.getNotesByIds(noteIds)
            _currentIndex.value = 0
            _isComplete.value = _dueCards.value.isEmpty()
        }
    }

    fun rateCard(rating: Int) {
        val cards = _dueCards.value
        val idx = _currentIndex.value
        if (idx >= cards.size) return
        val card = cards[idx]
        viewModelScope.launch {
            val stability = card.stability
            val difficulty = card.difficulty
            val retrievability = FSRScheduler.forgettingCurve(card.interval.toDouble(), stability)
            val newDifficulty = FSRScheduler.nextDifficulty(difficulty, rating)
            val newStability = if (rating == 1) {
                FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
            } else {
                FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
            }
            val newInterval = FSRScheduler.nextIntervalFSRS(newStability)
            quizRepository.updateCard(
                card.copy(
                    interval = newInterval,
                    difficulty = newDifficulty,
                    stability = newStability
                )
            )
            val nextIndex = idx + 1
            if (nextIndex >= cards.size) {
                _isComplete.value = true
            } else {
                _currentIndex.value = nextIndex
            }
        }
    }

    fun getNextIntervalsForCurrentCard(): List<Int> {
        val cards = _dueCards.value
        val idx = _currentIndex.value
        if (idx >= cards.size) return emptyList()
        val card = cards[idx]
        val stability = card.stability
        val difficulty = card.difficulty
        val retrievability = FSRScheduler.forgettingCurve(card.interval.toDouble(), stability)
        return (1..4).map { rating ->
            val newStability = if (rating == 1) {
                FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
            } else {
                FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
            }
            FSRScheduler.nextIntervalFSRS(newStability)
        }
    }
}
