package com.example.studyio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardRepository
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckRepository
import com.example.studyio.data.entities.DeckState
import com.example.studyio.events.Events
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository
) : ViewModel() {
    private val _activeDecks = MutableStateFlow<List<Deck>>(emptyList())
    private val _archivedDecks = MutableStateFlow<List<Deck>>(emptyList())
    private val _cardCountMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _selectedTab = MutableStateFlow(DeckTab.ACTIVE)
    
    val activeDecks: StateFlow<List<Deck>> = _activeDecks
    val archivedDecks: StateFlow<List<Deck>> = _archivedDecks
    val cardCountMap: StateFlow<Map<String, Int>> = _cardCountMap
    val selectedTab: StateFlow<DeckTab> = _selectedTab

    init {
        loadDecks()
        viewModelScope.launch {
            Events.deckUpdated.collectLatest {
                loadDecks()
            }
        }
    }

    fun setSelectedTab(tab: DeckTab) {
        _selectedTab.value = tab
    }

    private fun fetchCardCounts() {
        viewModelScope.launch {
            val allVisibleDecks = _activeDecks.value + _archivedDecks.value
            val deckCounts = mutableMapOf<String, Int>()
            allVisibleDecks.forEach { deck ->
                val count = deckRepository.getDueCardsCount(deck.id)
                deckCounts[deck.id] = count
            }
            _cardCountMap.value = deckCounts
        }
    }

    fun loadDecks() {
        viewModelScope.launch {
            _activeDecks.value = deckRepository.getActiveDecks()
            _archivedDecks.value = deckRepository.getArchivedDecks()
            fetchCardCounts()
        }
    }

    fun createDeck(deck: Deck, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
            Events.decksUpdated()
            onComplete?.invoke()
        }
    }
    
    fun updateDeck(deck: Deck, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.updateDeck(deck)
            loadDecks()
            Events.decksUpdated()
            onComplete?.invoke()
        }
    }

    fun createDeckWithCards(deck: Deck, cards: List<Pair<String, String>>) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
            cards.forEach { (front, back) ->
                val card = Card(
                    deckId = deck.id,
                    front = front,
                    back = back,
                )
                cardRepository.insertCard(card)
            }
            Events.decksUpdated()
        }
    }

    fun deleteDeck(deckId: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.softDeleteDeck(deckId)
            loadDecks()
            Events.decksUpdated()
            onComplete?.invoke()
        }
    }

    fun toggleDeckArchiveStatus(deck: Deck) {
        viewModelScope.launch {
            deckRepository.toggleDeckArchiveStatus(deck.id)
            loadDecks()
            Events.decksUpdated()
        }
    }

    fun updateDeckSchedule(deckId: String, schedule: Int) {
        viewModelScope.launch {
            deckRepository.getDeckById(deckId)?.let { deck ->
                val updatedDeck = deck.copy(studySchedule = schedule)
                deckRepository.updateDeck(updatedDeck)
                loadDecks()
                Events.decksUpdated()
            }
        }
    }

    suspend fun getDeckById(deckId: String): Deck? {
        return deckRepository.getDeckById(deckId)
    }
}

enum class DeckTab {
    ACTIVE, ARCHIVED
}

