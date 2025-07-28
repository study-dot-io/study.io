package com.example.studyio.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.CardRepository
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards
    
    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck

    suspend fun loadCards(deckId: String) {
            val allCards = cardRepository.getCardsByDeckId(deckId)
            // Sort cards by due date (earliest first)
            _cards.value = allCards.sortedBy { it.due }
        }
    
    fun loadDeck(deckId: String) {
        viewModelScope.launch {
            _deck.value = deckRepository.getDeckById(deckId)
        }
    }
}
