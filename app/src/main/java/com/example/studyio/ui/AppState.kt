package com.example.studyio.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.studyio.data.entities.Deck
import java.time.LocalDateTime

sealed class Screen {
    object Home : Screen()
    object CreateDeck : Screen()
    data class DeckDetail(val deckId: Long) : Screen()
    data class Quiz(val deckId: Long) : Screen()
}

data class AppState(
    val currentScreen: Screen = Screen.Home,
    val decks: List<Deck> = emptyList()
) {
    fun navigateTo(screen: Screen): AppState = copy(currentScreen = screen)
    
    fun addDeck(deck: Deck): AppState {
        val newDeck = deck.copy(id = (decks.maxOfOrNull { it.id } ?: 0) + 1)
        return copy(decks = decks + newDeck)
    }
    
    val totalCards: Int get() = 0 // Placeholder, should be calculated from cards table
    val totalDecks: Int get() = decks.size
    val dueCards: Int get() = 23 // Mock data for now
    val todayReviews: Int get() = 45 // Mock data for now
}