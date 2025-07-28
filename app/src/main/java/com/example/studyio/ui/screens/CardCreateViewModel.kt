package com.example.studyio.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Card
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.CardRepository
import com.example.studyio.data.entities.CardType
import com.example.studyio.data.entities.DeckRepository
import com.example.studyio.events.Events
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardCreateViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) : ViewModel() {
    private val _availableDecks = MutableStateFlow<List<Deck>>(emptyList())
    val availableDecks: StateFlow<List<Deck>> = _availableDecks

    fun loadDecks() {
        viewModelScope.launch {
            _availableDecks.value = deckRepository.getAllDecks()
        }
    }

    fun createCard(deckId: String, front: String, back: String, tags: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val card = Card(
                deckId = deckId,
                type = CardType.NEW,
                front = front,
                back = back,
                tags = tags
            )
            try {
                cardRepository.insertCard(card)
                Events.decksUpdated()
                onDone()
            } catch (e: Exception) {
                Log.e("CardCreateViewModel", "Error creating card", e)
            }
        }
    }
}
