package com.example.studyio.data.repository

import com.example.studyio.data.db.DAO.DeckDao
import com.example.studyio.data.model.Deck
import javax.inject.Inject

class DeckRepository (private val deckDao: DeckDao)  {
    suspend fun getAlldecks(): List<Deck> {
        return deckDao.getAllDecks()
    }
    suspend fun insertDeck(deck: Deck) {
        deckDao.insert(deck)
    }

    suspend fun deleteDeck(deck: Deck) {
        deckDao.delete(deck)
    }

    suspend fun getDeckById(id: Long): Deck? {
        return deckDao.getDeckById(id)
    }

    // Dummy data
    suspend fun insertDummyData() {
        val deck1 = Deck(name = "Deck 1", description = "Description 1", cardCount = 1)
        val deck2 = Deck(name = "Deck 2", description = "Description 2", cardCount = 2)
        insertDeck(deck1)
        insertDeck(deck2)
    }
}