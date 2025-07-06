package com.example.studyio.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Note
import com.example.studyio.data.entities.NoteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    fun loadNotes(deckId: Long) {
        viewModelScope.launch {
            _notes.value = noteDao.getNotesForDeck(deckId)
        }
    }
}

