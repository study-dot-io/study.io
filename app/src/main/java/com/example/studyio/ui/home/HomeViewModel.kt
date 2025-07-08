package com.example.studyio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckRepository
import com.example.studyio.events.Events
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
    private val _cardCountMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val decks: StateFlow<List<Deck>> = _decks
    val cardCountMap: StateFlow<Map<String, Int>> = _cardCountMap

    init {
        loadDecks()
        viewModelScope.launch {
            Events.deckUpdated.collectLatest {
                loadDecks()
            }
        }
    }

    fun loadDecks() {
        viewModelScope.launch {
            _decks.value = deckRepository.getAllDecks()
            // Fetch card counts to enhance the deck information; TODO: make this more efficient if this becomes a bottleneck
            val deckCounts = mutableMapOf<String, Int>()
            _decks.value.forEach { deck ->
                val count = deckRepository.getDueCardsCount(deck.id)
                deckCounts[deck.id] = count
            }
            
            _cardCountMap.value = deckCounts
        }
    }

    fun createDeck(deck: Deck, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
            loadDecks()
            onComplete?.invoke()
        }
    }

    fun deleteDeck(deckId: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
            loadDecks()
            onComplete?.invoke()
        }
    }
}
