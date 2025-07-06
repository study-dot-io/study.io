package com.example.studyio.data

import com.example.studyio.data.entities.Deck
import com.example.studyio.data.entities.DeckDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepository @Inject constructor(private val deckDao: DeckDao) {
    suspend fun getAllDecks(): List<Deck> = deckDao.getAllDecks()
    suspend fun insertDeck(deck: Deck): Long = deckDao.insertDeck(deck)
    suspend fun deleteDeck(deckId: Long) = deckDao.deleteDeck(deckId)
}

