package com.example.studyio.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckRepository
import com.example.studyio.data.entities.QuizSessionRepository
import com.example.studyio.events.Events
import com.example.studyio.utils.StudyScheduleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * DTO for enhanced deck information including computed fields
 */
data class DeckInfo(
    val deck: Deck,
    val dueCardsCount: Int,
    val hasCompletedToday: Boolean
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val quizSessionRepository: QuizSessionRepository
) : ViewModel() {
    private val _activeDecks = MutableStateFlow<List<DeckInfo>>(emptyList())
    private val _archivedDecks = MutableStateFlow<List<DeckInfo>>(emptyList())
    private val _selectedTab = MutableStateFlow(DeckTab.ACTIVE)
    
    val activeDecks: StateFlow<List<DeckInfo>> = _activeDecks
    val archivedDecks: StateFlow<List<DeckInfo>> = _archivedDecks
    val selectedTab: StateFlow<DeckTab> = _selectedTab

    init {
        loadDecks()
        viewModelScope.launch {
            Events.deckUpdated.collectLatest {
                loadDecks()
            }
            Events.quizCompleted.collectLatest { 
                loadDecks() // Mark quiz completion
            }
        }
    }

    fun setSelectedTab(tab: DeckTab) {
        _selectedTab.value = tab
    }

    fun loadDecks() {
        Log.w("HomeViewModel", "Loading decks")
        viewModelScope.launch {
            _activeDecks.value = createDeckInfoList(deckRepository.getActiveDecks())
            _archivedDecks.value = createDeckInfoList(deckRepository.getArchivedDecks())
        }
    }

    private suspend fun createDeckInfoList(decks: List<Deck>): List<DeckInfo> {
        return decks.map { deck ->
            val dueCardsCount = deckRepository.getDueCardsCount(deck.id)
            val lastCompletedDate = quizSessionRepository.getLastCompletedSessionDate(deck.id)
            val hasCompletedToday = lastCompletedDate?.let {
                StudyScheduleUtils.isCurrentDay(it)
            } == true
            DeckInfo(
                deck = deck,
                dueCardsCount = dueCardsCount,
                hasCompletedToday = hasCompletedToday
            )
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
            Events.decksUpdated()
            onComplete?.invoke()
        }
    }

    fun deleteDeck(deckId: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            deckRepository.softDeleteDeck(deckId)
            Events.decksUpdated()
            onComplete?.invoke()
        }
    }

}

enum class DeckTab {
    ACTIVE, ARCHIVED
}



