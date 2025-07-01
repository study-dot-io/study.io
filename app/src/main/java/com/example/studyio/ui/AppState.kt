package com.example.studyio.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.studyio.data.entities.Deck
import java.time.LocalDateTime

sealed class Screen {
    object Home : Screen()
    object CreateDeck : Screen()
}

data class AppState(
    val currentScreen: Screen = Screen.Home,
    val decks: List<Deck> = listOf(
        Deck(
            id = 1,
            name = "Biology Fundamentals",
            description = "Basic concepts in biology",
            cardCount = 150,
            lastStudied = LocalDateTime.now().minusDays(1),
            color = "#10B981"
        ),
        Deck(
            id = 2,
            name = "Spanish Vocabulary",
            description = "Essential Spanish words and phrases",
            cardCount = 89,
            lastStudied = LocalDateTime.now().minusHours(3),
            color = "#F59E20"
        ),
        Deck(
            id = 3,
            name = "Math Formulas",
            description = "Important mathematical formulas",
            cardCount = 67,
            lastStudied = LocalDateTime.now().minusDays(2),
            color = "#6366F1"
        ),
        Deck(
            id = 4,
            name = "History Timeline",
            description = "Key historical events",
            cardCount = 234,
            lastStudied = LocalDateTime.now().minusDays(5),
            color = "#EF4444"
        )
    )
) {
    fun navigateTo(screen: Screen): AppState = copy(currentScreen = screen)
    
    fun addDeck(deck: Deck): AppState {
        val newDeck = deck.copy(id = (decks.maxOfOrNull { it.id } ?: 0) + 1)
        return copy(decks = decks + newDeck)
    }
    
    val totalCards: Int get() = decks.sumOf { it.cardCount }
    val totalDecks: Int get() = decks.size
    val dueCards: Int get() = 23 // Mock data for now
    val todayReviews: Int get() = 45 // Mock data for now
}