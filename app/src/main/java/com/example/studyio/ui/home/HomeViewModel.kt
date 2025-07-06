package com.example.studyio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.DeckRepository
import com.example.studyio.data.entities.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {
    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            _decks.value = deckRepository.getAllDecks()
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

