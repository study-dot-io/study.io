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
            val todayEpoch = LocalDate.now().toEpochDay().toInt()
            val stability = card.stability
            val difficulty = card.difficulty
            val retrievability = FSRScheduler.forgettingCurve(card.interval.toDouble(), stability)
            val isLearning = FSRScheduler.isLearning(card)
            val isNew = FSRScheduler.isNew(card)
            val isReview = FSRScheduler.isReview(card)
            var newInterval: Int
            var newDue: Int
            var newDifficulty: Double
            var newStability: Double
            if (isLearning || isNew) {
                // Short-term learning steps for new/learning cards with minute/hour precision
                val now = java.time.LocalDateTime.now()
                when (rating) {
                    1 -> { // 'Again' - due now
                        newInterval = 0
                        newDue = todayEpoch
                        newDifficulty = difficulty
                        newStability = stability
                        quizRepository.updateCard(
                            card.copy(
                                interval = newInterval,
                                difficulty = newDifficulty,
                                stability = newStability,
                                due = newDue,
                                createdAt = now
                            )
                        )
                        val nextIndex = idx + 1
                        if (nextIndex >= cards.size) {
                            _isComplete.value = true
                        } else {
                            _currentIndex.value = nextIndex
                        }
                        return@launch
                    }
                    2 -> { // 'Hard' - 10 minutes
                        newInterval = 0
                        newDue = todayEpoch
                        newDifficulty = difficulty
                        newStability = stability
                        val dueTime = now.plusMinutes(10)
                        quizRepository.updateCard(
                            card.copy(
                                interval = newInterval,
                                difficulty = newDifficulty,
                                stability = newStability,
                                due = newDue,
                                createdAt = dueTime
                            )
                        )
                        val nextIndex = idx + 1
                        if (nextIndex >= cards.size) {
                            _isComplete.value = true
                        } else {
                            _currentIndex.value = nextIndex
                        }
                        return@launch
                    }
                    3 -> { // 'Good' - 1 hour
                        newInterval = 0
                        newDue = todayEpoch
                        newDifficulty = difficulty
                        newStability = stability
                        val dueTime = now.plusHours(1)
                        quizRepository.updateCard(
                            card.copy(
                                interval = newInterval,
                                difficulty = newDifficulty,
                                stability = newStability,
                                due = newDue,
                                createdAt = dueTime
                            )
                        )
                        val nextIndex = idx + 1
                        if (nextIndex >= cards.size) {
                            _isComplete.value = true
                        } else {
                            _currentIndex.value = nextIndex
                        }
                        return@launch
                    }
                    4 -> { // 'Easy' - graduate to review (1 day)
                        newDifficulty = FSRScheduler.nextDifficulty(difficulty, rating)
                        newStability = FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                        newInterval = 1 // 1 day
                        newDue = todayEpoch + newInterval
                    }
                    else -> {
                        newInterval = 0
                        newDue = todayEpoch
                        newDifficulty = difficulty
                        newStability = stability
                    }
                }
            } else if (isReview) {
                newDifficulty = FSRScheduler.nextDifficulty(difficulty, rating)
                newStability = if (rating == 1) {
                    FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
                } else {
                    FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                }
                newInterval = FSRScheduler.nextIntervalFSRS(newStability)
                newDue = todayEpoch + newInterval
            } else {
                // Fallback: treat as review
                newDifficulty = FSRScheduler.nextDifficulty(difficulty, rating)
                newStability = if (rating == 1) {
                    FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
                } else {
                    FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                }
                newInterval = FSRScheduler.nextIntervalFSRS(newStability)
                newDue = todayEpoch + newInterval
            }
            quizRepository.updateCard(
                card.copy(
                    interval = newInterval,
                    difficulty = newDifficulty,
                    stability = newStability,
                    due = newDue
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
        val isLearning = FSRScheduler.isLearning(card)
        val isNew = FSRScheduler.isNew(card)
        return (1..4).map { rating ->
            if (isLearning || isNew) {
                if (rating == 1) {
                    // 'Again' for learning/new: due today (interval 0)
                    0
                } else {
                    val newStability = FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                    FSRScheduler.nextIntervalFSRS(newStability)
                }
            } else {
                val newStability = if (rating == 1) {
                    FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
                } else {
                    FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                }
                FSRScheduler.nextIntervalFSRS(newStability)
            }
        }
    }

    fun getNextIntervalsForCurrentCardFormatted(): List<String> {
        val cards = _dueCards.value
        val idx = _currentIndex.value
        if (idx >= cards.size) return emptyList()
        val card = cards[idx]
        val stability = card.stability
        val difficulty = card.difficulty
        val retrievability = FSRScheduler.forgettingCurve(card.interval.toDouble(), stability)
        val isLearning = FSRScheduler.isLearning(card)
        val isNew = FSRScheduler.isNew(card)
        return (1..4).map { rating ->
            val minutes = when {
                isLearning || isNew -> {
                    // Simulate a short learning step (e.g., 10min, 1hr, 1d)
                    // You can adjust these as needed for your app's learning steps
                    when (rating) {
                        1 -> 0 // 'Again' - due now
                        2 -> 10 // Hard: 10 minutes
                        3 -> 60 // Good: 1 hour
                        4 -> 1440 // Easy: 1 day
                        else -> 0
                    }
                }
                else -> {
                    val newStability = if (rating == 1) {
                        FSRScheduler.nextForgetStability(difficulty, stability, retrievability)
                    } else {
                        FSRScheduler.nextRecallStability(difficulty, stability, retrievability, rating)
                    }
                    val intervalDays = FSRScheduler.nextIntervalFSRS(newStability)
                    intervalDays * 1440 // convert days to minutes
                }
            }
            formatMinutesToHumanReadable(minutes)
        }
    }

    private fun formatMinutesToHumanReadable(minutes: Int): String {
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "$minutes min"
            minutes < 1440 -> "${minutes / 60} hr${if (minutes >= 120) "s" else ""}"
            else -> "${minutes / 1440} day${if (minutes >= 2880) "s" else ""}"
        }
    }
}
