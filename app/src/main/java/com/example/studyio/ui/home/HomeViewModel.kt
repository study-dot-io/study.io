package com.example.studyio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.DeckRepository
import com.example.studyio.data.entities.Deck
import com.example.studyio.events.QuizEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {
    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    private val _deckCountMap = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val decks: StateFlow<List<Deck>> = _decks
    val deckCountMap: StateFlow<Map<Long, Int>> = _deckCountMap

    init {
        loadDecks()
        viewModelScope.launch {
            QuizEvents.quizCompleted.collectLatest {
                loadDecks()
            }
        }
    }

    fun loadDecks() {
        viewModelScope.launch {
            _decks.value = deckRepository.getAllDecks()
            // Fetch card counts to enhance the deck information; TODO: make this more efficient
            val deckCounts = mutableMapOf<Long, Int>()
            _decks.value.forEach { deck ->
                val count = deckRepository.getDueCardsCount(deck.id)
                deckCounts[deck.id] = count
            }
            
            _deckCountMap.value = deckCounts
        }
    }

    fun createDeck(deck: Deck, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
            loadDecks()
            onComplete?.invoke()
        }
    }

    fun deleteDeck(deckId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
            loadDecks()
            onComplete?.invoke()
        }
    }
}
