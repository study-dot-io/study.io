package com.example.studyio.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.QuizRepository
import com.example.studyio.data.entities.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DeckWithDueCount(
    val deck: Deck,
    val dueCount: Int
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val deckDao: com.example.studyio.data.entities.DeckDao
) : ViewModel() {
    private val _decksWithDueCount = MutableStateFlow<List<DeckWithDueCount>>(emptyList())
    val decksWithDueCount: StateFlow<List<DeckWithDueCount>> = _decksWithDueCount

    init {
        loadDecksWithDueCount()
    }

    fun loadDecksWithDueCount() {
        viewModelScope.launch {
            val decks = deckDao.getAllDecks()
            val todayEpoch = LocalDate.now().toEpochDay().toInt()
            val result = decks.map { deck ->
                val dueCount = quizRepository.getDueCardCount(deck.id, todayEpoch)
                DeckWithDueCount(deck, dueCount)
            }
            _decksWithDueCount.value = result
        }
    }
}

