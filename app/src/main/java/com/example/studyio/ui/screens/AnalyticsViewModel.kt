package com.example.studyio.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.CardRepository
import com.example.studyio.data.entities.QuizQuestionRepository
import com.example.studyio.data.entities.QuizSessionRepository
import com.example.studyio.data.entities.CardRating
import com.example.studyio.data.entities.DeckReviewCount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val quizSessionRepository: QuizSessionRepository,
    private val quizQuestionRepository: QuizQuestionRepository
) : ViewModel() {

    private val _totalCardsReviewed = MutableStateFlow(0)
    val totalCardsReviewed: StateFlow<Int> = _totalCardsReviewed

    private val _totalCardsCreated = MutableStateFlow(0)
    val totalCardsCreated: StateFlow<Int> = _totalCardsCreated

    private val _averageRating = MutableStateFlow(0f)
    val averageRating: StateFlow<Float> = _averageRating

    private val _cardsReviewed = MutableStateFlow(0)
    val cardsReviewed: StateFlow<Int> = _cardsReviewed

    private val _worstRatedCards = MutableStateFlow(emptyList<CardRating>())
    val worstRatedCards: StateFlow<List<CardRating>> = _worstRatedCards

    private val _mostReviewedDecks = MutableStateFlow(emptyList<DeckReviewCount>())
    val mostReviewedDecks: StateFlow<List<DeckReviewCount>> = _mostReviewedDecks

    init {
        fetchAnalyticsData()
    }

    private fun fetchAnalyticsData() {
        viewModelScope.launch {
            _totalCardsReviewed.value = quizQuestionRepository.getTotalCardsReviewed()
            _totalCardsCreated.value = cardRepository.getTotalCardsCreated()
            _averageRating.value = quizQuestionRepository.getAverageRating()
            _cardsReviewed.value = quizQuestionRepository.getCardsReviewedLastMonth()
            _worstRatedCards.value = quizQuestionRepository.getWorstRatedCards()
            _mostReviewedDecks.value = quizSessionRepository.getMostReviewedDecks()
        }
    }
}
