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
}

data class AppState(
    val currentScreen: Screen = Screen.Home,
    val decks: List<Deck> = listOf(
        Deck(
            id = 1,
            name = "Biology Fundamentals",
            description = "Basic concepts in biology",
            color = "#10B981"
        ),
        Deck(
            id = 2,
            name = "Spanish Vocabulary",
            description = "Essential Spanish words and phrases",
            color = "#F59E20"
        ),
        Deck(
            id = 3,
            name = "Math Formulas",
            description = "Important mathematical formulas",
            color = "#6366F1"
        ),
        Deck(
            id = 4,
            name = "History Timeline",
            description = "Key historical events",
            color = "#EF4444"
        )
    )
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