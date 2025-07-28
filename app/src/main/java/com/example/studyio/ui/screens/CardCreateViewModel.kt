package com.example.studyio.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.CardType
import com.example.studyio.data.entities.DeckDao
import com.example.studyio.events.Events
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardCreateViewModel @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : ViewModel() {
    private val _availableDecks = MutableStateFlow<List<Deck>>(emptyList())
    val availableDecks: StateFlow<List<Deck>> = _availableDecks

    fun loadDecks() {
        viewModelScope.launch {
            _availableDecks.value = deckDao.getAllDecks()
        }
    }

    fun createCard(deckId: String, front: String, back: String, tags: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val card = com.example.studyio.data.entities.Card(
                deckId = deckId,
                type = CardType.NEW,
                front = front,
                back = back,
                tags = tags
            )
            viewModelScope.launch {
                cardDao.insertCard(card)
                Events.decksUpdated()
            }
            onDone()
        }
    }
}



