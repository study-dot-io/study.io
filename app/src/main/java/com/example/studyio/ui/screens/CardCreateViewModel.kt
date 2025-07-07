package com.example.studyio.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.CardDao
import com.example.studyio.data.entities.DeckDao
import com.example.studyio.data.entities.NoteDao
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
    private val noteDao: NoteDao
) : ViewModel() {
    private val _availableDecks = MutableStateFlow<List<Deck>>(emptyList())
    val availableDecks: StateFlow<List<Deck>> = _availableDecks

    fun loadDecks() {
        viewModelScope.launch {
            _availableDecks.value = deckDao.getAllDecks()
        }
    }

    fun createCard(deckId: Long, front: String, back: String, tags: String, onDone: () -> Unit) {
        viewModelScope.launch {
            // 1. Create Note
            val note = com.example.studyio.data.entities.Note.create(
                fields = listOf(front, back),
                tags = tags
            )
            val noteId = noteDao.insertNote(note)
            // 2. Create Card (ord=0 for basic card, type=0=new, queue=0, due=0)
            val card = com.example.studyio.data.entities.Card(
                deckId = deckId,
                noteId = noteId,
                ord = 0,
                type = 0,
                queue = 0,
                due = 0
            )
            cardDao.insertCard(card)
            Events.decksUpdated()
            onDone()
        }
    }
}
